package com.ht1.android.cgm;

public class CgmCommand {

	private static final byte[] READ_EGV_PAGE_RANGE_COMMAND = new byte[]{ 0x01, 0x07, 0x10, 0x04, (byte)0x8b, (byte)0xb8 };
	private static final int READ_EGV_PAGE_RANGE_RESPONSE_ARRAY_SIZE = 256;
	
	private static final int READ_FOUR_EGV_PAGES_COMMAND_ARRAY_SIZE = 636;
	private static final int READ_FOUR_EGV_PAGES_RESPONSE_ARRAY_SIZE = 2112;
	
	
	
	public static byte[] readEgvPageRange(){
		return READ_EGV_PAGE_RANGE_COMMAND;
	}
	
	public static byte[][] readEgvPages(byte[] pageRange, boolean lastFourOnly){
		return null;
	}
	
	
}
