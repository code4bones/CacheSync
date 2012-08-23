package com.google.cachesync;

public interface ICommandObj {

	public int Invoke() throws Exception;
	public void Reply(Object ... args) throws Exception;
	
}
