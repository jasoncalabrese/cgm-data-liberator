package com.ht1.android.cgm;

import java.nio.ByteBuffer;

public class CgmCommand {

	public static final int READ_EGV_PAGE_RANGE_RESPONSE_ARRAY_SIZE = 256;
	public static final int WRITE_EGV_PAGE_RANGE_RESPONSE_TIMEOUT_MS = 200;
	public static final int READ_EGV_PAGE_RANGE_RESPONSE_TIMEOUT_MS = 2000;
	
	public static final int READ_FOUR_EGV_PAGES_COMMAND_ARRAY_SIZE = 636;
	public static final int READ_FOUR_EGV_PAGES_RESPONSE_ARRAY_SIZE = 2122;
	public static final int FOUR_EGV_PAGES_ARRAY_SIZE = 2112;
	public static final int WRITE_EGV_PAGE_RESPONSE_TIMEOUT_MS = 200;
	public static final int READ_EGV_PAGE_RESPONSE_TIMEOUT_MS = 20000;
	

	
	public static byte[][] readEgvPages(byte[] pageRange, boolean lastFourOnly){
		return null;
	}
	
	public static byte[] readEgvPageRangeCommand(){
        byte[] pageRangeCommand = new byte[7];
        pageRangeCommand[0] = 0x01;
        pageRangeCommand[1] = 0x07;		
        pageRangeCommand[3] = 0x10;
        pageRangeCommand[4] = 0x04;
        pageRangeCommand[5] = (byte)0x8b;
        pageRangeCommand[6] = (byte)0xb8;    
        return pageRangeCommand;
	}


	public static byte[] readEgvPageCommand(byte[] startPage, byte[] endPage) {
		
		//TODO actually implement creation of commands to read every page, not just the last four.
		
		int endInt = toInt(endPage, 1);
		int lastFour = endInt-3;
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(lastFour);
		byte[] result = buffer.array();

		byte[] readEgvPage = new byte[CgmCommand.READ_FOUR_EGV_PAGES_COMMAND_ARRAY_SIZE];
		readEgvPage[0] = 0x01;
		readEgvPage[1] = 0x0c;
		readEgvPage[2] = 0x00;
		readEgvPage[3] = 0x11;
		readEgvPage[4] = 0x04;
		readEgvPage[5] = result[3];
		readEgvPage[6] = result[2];
		readEgvPage[7] = result[1];
		readEgvPage[8] = result[0];
		readEgvPage[9] = 0x04;

		//Get checksum
		int getLastEGVCRC = calculateCRC16(readEgvPage, 0, 10);    
		byte crcByte1 = (byte) (getLastEGVCRC & 0xff);
		byte crcByte2 = (byte) ((getLastEGVCRC >> 8) & 0xff);

		readEgvPage[10] = crcByte1;
		readEgvPage[11] = crcByte2;
		
		return readEgvPage;
	}
	
	//Convert the packet data
	public static int toInt(byte[] b, int flag) {
		switch(flag){
			case 0: //BitConverter.FLAG_JAVA:
				return (int)(((b[0] & 0xff)<<24) | ((b[1] & 0xff)<<16) | ((b[2] & 0xff)<<8) | (b[3] & 0xff));
			case 1: //BitConverter.FLAG_REVERSE:
				return (int)(((b[3] & 0xff)<<24) | ((b[2] & 0xff)<<16) | ((b[1] & 0xff)<<8) | (b[0] & 0xff));
			default:
				throw new IllegalArgumentException("BitConverter:toInt");
		}
	}
	
	public static byte[] getBytes(int i, int flag) {
		byte[] b = new byte[4];
		switch (flag) {
		case 0:
			b[0] = (byte) ((i >> 24) & 0xff);
			b[1] = (byte) ((i >> 16) & 0xff);
			b[2] = (byte) ((i >> 8) & 0xff);
			b[3] = (byte) (i & 0xff);
			break;
		case 1:
			b[3] = (byte) ((i >> 24) & 0xff);
			b[2] = (byte) ((i >> 16) & 0xff);
			b[1] = (byte) ((i >> 8) & 0xff);
			b[0] = (byte) (i & 0xff);
			break;
		default:
			break;	
		}
		return b;
	}
	
	private static int calculateCRC16 (byte [] buff, int start, int end) {

        int crc = 0;
        for (int i = start; i < end; i++)
        {
        	
            crc = ((crc  >>> 8) | (crc  << 8) )& 0xffff;
            crc ^= (buff[i] & 0xff);
            crc ^= ((crc & 0xff) >> 4);
            crc ^= (crc << 12) & 0xffff;
            crc ^= ((crc & 0xFF) << 5) & 0xffff;

        }
        crc &= 0xffff;
        return crc;
    	
    }
}
