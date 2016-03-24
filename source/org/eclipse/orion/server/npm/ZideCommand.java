package org.eclipse.orion.server.npm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * $Id$
 * @author gramkumar-0817
 *
 */

public class ZideCommand {

	private final int DEFAULT_TIME_OUT = 360000;
	private String command;
	private List<String> options;
	private File workingDir;

	private int exitCode;
	
	private ProcessWrapper wrapper;
	
	public ZideCommand() {
		this(null);
	}
	
	public ZideCommand(String command, File workingDir) {
		this(workingDir);
		this.command = command;		
	}
	
	public ZideCommand(File workingDir) {
		this.workingDir = workingDir;
		this.options = new ArrayList<String>();		
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}
	
	public int getExitCode() {
		return exitCode;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	public void addOptions(String...arguments) {
		for (String option : arguments) {
			options.add(option);
		}
	}
	
	public void addOptions(List<String> options) {
		this.options.addAll(options);
	}
	
	public byte[] execute() {
		return execute(DEFAULT_TIME_OUT);
	}
	
	private List<String> getCommand() {
		List<String> output = new ArrayList<String>();
		output.add(command);
		output.addAll(options);
		return output;
	}
	
	private ProcessWrapper getProcessWrapper(ProcessBuilder pb, OutputStream os) {
		return new ProcessWrapper(pb, os);
	}	
	
	private void waitForTimeout(int timeout) throws InterruptedException {
		if(timeout < 0) {
			timeout = 1;
		}
		
		long startTime = System.currentTimeMillis();		
		while(wrapper.isAlive()) {
			synchronized (this) {
				wait(10);
			}
			long currentTime = System.currentTimeMillis();
			long delay = currentTime - startTime;
			if(delay > timeout) {
				break;
			}
		}		
	}
	
	public byte[] execute(int timeout) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();		
		try {
			List<String> cmd = getCommand();
			ProcessBuilder pb = new ProcessBuilder(cmd);
			if(workingDir != null) {
				pb.directory(workingDir);
			}
			
			wrapper = getProcessWrapper(pb, bos);
			wrapper.start();
			
			waitForTimeout(timeout);
			setExitCode(wrapper.getExitCode());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	
	public String executeToString() {
		byte[] outputbytes = execute();
		if(getExitCode() == 0) {
			return new String(outputbytes);
		}
		return null;
	}

}
