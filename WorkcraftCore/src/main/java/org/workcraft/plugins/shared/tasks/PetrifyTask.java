package org.workcraft.plugins.shared.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DataAccumulator;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class PetrifyTask implements Task<ExternalProcessResult>, ExternalProcessListener {
		private String[] args;
		private String inputFileName;
		
		private volatile boolean finished;
		private volatile int returnCode;
		private boolean userCancelled = false;
		private ProgressMonitor<? super ExternalProcessResult> monitor;
		
		private DataAccumulator stdoutAccum = new DataAccumulator();
		private DataAccumulator stderrAccum = new DataAccumulator();
				
		public PetrifyTask(String[] args, String inputFileName) {
			this.args = args;
			this.inputFileName = inputFileName;
		}
		
		@Override
		public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
			this.monitor = monitor;
			
			ArrayList<String> command = new ArrayList<String>();
			command.add(eval(PetrifyUtilitySettings.petrifyCommand));
			
			for (String arg : eval(PetrifyUtilitySettings.petrifyArgs).split(" "))
				if (!arg.isEmpty())
					command.add(arg);
			
			for (String arg : args)
				command.add(arg);
			
			command.add(inputFileName);
			
			ExternalProcess petrifyProcess = new ExternalProcess(command.toArray(new String[command.size()]), ".");
			
			petrifyProcess.addListener(this);
			
			try {
				petrifyProcess.start();
			} catch (IOException e) {
				return new Result<ExternalProcessResult>(e);
			}
			
			while (true) {
				if (monitor.isCancelRequested() && petrifyProcess.isRunning()) {
					petrifyProcess.cancel();
					userCancelled = true;
				}
				if (finished)
					break;
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					petrifyProcess.cancel();
					userCancelled = true;
					break;
				}
			}
			
			if (userCancelled)
				return new Result<ExternalProcessResult>(Outcome.CANCELLED);
			
			ExternalProcessResult result = new ExternalProcessResult(returnCode, stdoutAccum.getData(), stderrAccum.getData());
			
			if (returnCode == 0)
				return new Result<ExternalProcessResult>(Outcome.FINISHED, result);
			
			return new Result<ExternalProcessResult>(Outcome.FAILED, result);
		}

		@Override
		public void errorData(byte[] data) {
			try {
				stderrAccum.write(data);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			monitor.stderr(data);
		}

		@Override
		public void outputData(byte[] data) {
			try {
				stdoutAccum.write(data);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			monitor.stdout(data);
		}

		@Override
		public void processFinished(int returnCode) {
			this.returnCode = returnCode;
			this.finished = true;
		}
	}