package com.clipsoft.cson;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

public class BinaryCSONBufferReader {
	
	private final static Charset UTF8 = StandardCharsets.UTF_8;

	public final static void parse(byte[] buffer, ParseCallback callback) {
		parse(buffer,0, buffer.length, callback);
	}
	public final static void parse(byte[] buffer,int offset, int len, ParseCallback callback) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, len);
		ArrayDeque<Byte> TypeStack = new ArrayDeque<Byte>();
		TypeStack.addLast(BinaryCSONDataType.TYPE_NULL);
		byte prefix = 0;
		byte[] version = new byte[BinaryCSONDataType.VER_RAW.length];
		prefix = byteBuffer.get();
		byteBuffer.get(version, 0, version.length);
		
		if(prefix != BinaryCSONDataType.PREFIX) {
			throw new CSONParseException("CSON Prefix does not match.");
		}

		callback.onVersion(version);
 		byte type = byteBuffer.get(); 
		if(type == BinaryCSONDataType.TYPE_OPEN_OBJECT) {
			callback.onOpenObject();
			TypeStack.addLast(BinaryCSONDataType.TYPE_OPEN_OBJECT);
			read(true,TypeStack, byteBuffer, callback);
		}
		else if(type == BinaryCSONDataType.TYPE_OPEN_ARRAY) {
			callback.onOpenArray();
			TypeStack.addLast(BinaryCSONDataType.TYPE_OPEN_ARRAY);
			read(false,TypeStack, byteBuffer, callback);
		}
		
 		
	}
	
	
	public final static void read(boolean isReadObject, ArrayDeque<Byte> typeStack, ByteBuffer byteBuffer, ParseCallback callback) {
		
		while(true) {
			byte type = byteBuffer.get();
			if(type == -1) return;
			if(isReadObject) {
				if(type == BinaryCSONDataType.TYPE_CLOSE_OBJECT) {
					callback.onCloseObject();
					typeStack.pollLast();
					byte topType = typeStack.getLast();
					if(topType == BinaryCSONDataType.TYPE_OPEN_ARRAY) {
						isReadObject = false;
						continue;
					}
					else if(topType == BinaryCSONDataType.TYPE_OPEN_OBJECT) {
						isReadObject = true;
						continue;
					} else if(typeStack.size() == 1 && topType == BinaryCSONDataType.TYPE_NULL) {
						return;
					}
				}
				byte rawType = (byte)(type & 0xF0);
				switch(rawType) {
					case BinaryCSONDataType.TYPE_STRING_SHORT :
					case BinaryCSONDataType.TYPE_STRING_MIDDLE :
					case BinaryCSONDataType.TYPE_STRING_LONG :
						String key = readString(type, rawType, byteBuffer);
						if(key == null) {
							throw new CSONParseException();
						}
						callback.onKey(key);
						break;
					default:
						throw new CSONParseException();
				}
				byte valueType = byteBuffer.get();
				switch (valueType) {
				case BinaryCSONDataType.TYPE_OPEN_OBJECT:
					callback.onOpenObject();
					typeStack.add(BinaryCSONDataType.TYPE_OPEN_OBJECT);
					isReadObject = true;
					continue;
				case BinaryCSONDataType.TYPE_OPEN_ARRAY:
					callback.onOpenArray();
					typeStack.add(BinaryCSONDataType.TYPE_OPEN_ARRAY);
					isReadObject = false;
					continue;
				case BinaryCSONDataType.TYPE_CLOSE_OBJECT:
					callback.onCloseObject();
					typeStack.pollLast();
					byte topType = typeStack.getLast();
					if(topType == BinaryCSONDataType.TYPE_OPEN_ARRAY) {
						isReadObject = false;
						continue;
					}
					else if(topType == BinaryCSONDataType.TYPE_OPEN_OBJECT) {
						isReadObject = true;
						continue;
					}
					return;
				case BinaryCSONDataType.TYPE_NULL:
					callback.onValue(readValue(valueType, byteBuffer));
					isReadObject = true;
					continue;
				case BinaryCSONDataType.TYPE_BYTE:
				case BinaryCSONDataType.TYPE_BOOLEAN:
				case BinaryCSONDataType.TYPE_CHAR:
				case BinaryCSONDataType.TYPE_SHORT:
				case BinaryCSONDataType.TYPE_INT:
				case BinaryCSONDataType.TYPE_FLOAT:
				case BinaryCSONDataType.TYPE_LONG:
				case BinaryCSONDataType.TYPE_DOUBLE:
				case BinaryCSONDataType.TYPE_BIGDECIMAL:
				//case BinaryCSONDataType.TYPE_NULL:
					callback.onValue(readValue(valueType, byteBuffer));
					isReadObject = true;
					continue;
				default:
					byte rawTypeValue = (byte)(valueType & 0xF0);
					switch (rawTypeValue) {
					case BinaryCSONDataType.TYPE_STRING_SHORT :
					case BinaryCSONDataType.TYPE_STRING_MIDDLE :
					case BinaryCSONDataType.TYPE_STRING_LONG :
					case BinaryCSONDataType.TYPE_RAW_MIDDLE:
					case BinaryCSONDataType.TYPE_RAW_LONG:
					case BinaryCSONDataType.TYPE_RAW_WILD:
						callback.onValue(readValue(valueType, byteBuffer));
						isReadObject = true;
						continue;
					}
					throw new CSONParseException();
				}
			} else {
				if(type == BinaryCSONDataType.TYPE_CLOSE_ARRAY) {
					callback.onCloseArray();
					typeStack.pollLast();
					byte topType = typeStack.getLast();
					if(topType == BinaryCSONDataType.TYPE_OPEN_ARRAY) {
						isReadObject = false;
						continue;
					}
					else if(topType == BinaryCSONDataType.TYPE_OPEN_OBJECT) {
						isReadObject = true;
						continue;
					}  else if(typeStack.size() == 1 && topType == BinaryCSONDataType.TYPE_NULL) {
						return;
					}
					return;
				}
				switch (type) {
				case BinaryCSONDataType.TYPE_OPEN_OBJECT:
					callback.onOpenObject();
					typeStack.addLast(BinaryCSONDataType.TYPE_OPEN_OBJECT);
					isReadObject = true;
					continue;
				case BinaryCSONDataType.TYPE_OPEN_ARRAY:
					callback.onOpenArray();
					typeStack.addLast(BinaryCSONDataType.TYPE_OPEN_ARRAY);
					isReadObject = false;
					continue;
				case BinaryCSONDataType.TYPE_CLOSE_ARRAY:
					callback.onCloseArray();
		 			typeStack.pollLast();
		  			byte topType = typeStack.getLast();
					if(topType == BinaryCSONDataType.TYPE_OPEN_ARRAY) {
						isReadObject = false;
						continue;
					}
					else if(topType == BinaryCSONDataType.TYPE_OPEN_OBJECT) {
						isReadObject = true;
						continue;
					}
				case BinaryCSONDataType.TYPE_NULL:
					callback.onValue(readValue(type, byteBuffer));
					isReadObject = false;
					continue;
				case BinaryCSONDataType.TYPE_BYTE:
				case BinaryCSONDataType.TYPE_BOOLEAN:
				case BinaryCSONDataType.TYPE_CHAR:
				case BinaryCSONDataType.TYPE_SHORT:
				case BinaryCSONDataType.TYPE_INT:
				case BinaryCSONDataType.TYPE_FLOAT:
				case BinaryCSONDataType.TYPE_LONG:
				case BinaryCSONDataType.TYPE_DOUBLE:
				case BinaryCSONDataType.TYPE_BIGDECIMAL:

					callback.onValue(readValue(type, byteBuffer));
					isReadObject = false;
					continue;
				default:
					byte rawType = (byte)(type & 0xF0);
					switch (rawType) {
						case BinaryCSONDataType.TYPE_STRING_SHORT :
						case BinaryCSONDataType.TYPE_STRING_MIDDLE :
						case BinaryCSONDataType.TYPE_STRING_LONG :
						case BinaryCSONDataType.TYPE_RAW_MIDDLE:
						case BinaryCSONDataType.TYPE_RAW_LONG:
						case BinaryCSONDataType.TYPE_RAW_WILD:
						callback.onValue(readStreamType(type,rawType, byteBuffer));
						isReadObject = false;
						continue;
					}
					throw new CSONParseException();
				}
			}
		}
	}



	
	
	public final static Object readValue(byte type,ByteBuffer byteBuffer) {
		switch(type) {
			case BinaryCSONDataType.TYPE_BYTE:
				return byteBuffer.get();
			case BinaryCSONDataType.TYPE_BOOLEAN:
				return byteBuffer.get() == 1;
			case BinaryCSONDataType.TYPE_CHAR:
				return byteBuffer.getChar();
			case BinaryCSONDataType.TYPE_SHORT:
				return byteBuffer.getShort();
			case BinaryCSONDataType.TYPE_INT:
				return byteBuffer.getInt();
			case BinaryCSONDataType.TYPE_FLOAT:
				return byteBuffer.getFloat();
			case BinaryCSONDataType.TYPE_LONG:
				return byteBuffer.getLong();
			case BinaryCSONDataType.TYPE_DOUBLE:
				return byteBuffer.getDouble();
			case BinaryCSONDataType.TYPE_BIGDECIMAL:
				byte typeOfBigDecimal = byteBuffer.get();
				byte rawTypeOfBigDecimal = (byte)(typeOfBigDecimal & 0xF0);
				return new BigDecimal(readString(typeOfBigDecimal,rawTypeOfBigDecimal,byteBuffer));

			case BinaryCSONDataType.TYPE_NULL:
				return null;

			default:		
				byte rawtype = (byte)(type & 0xF0);
				return readStreamType(type,rawtype, byteBuffer);
		}
	}
	
	
	public final static Object readStreamType(byte type,byte rawtype, ByteBuffer byteBuffer) {
		switch (rawtype) {
		case BinaryCSONDataType.TYPE_STRING_SHORT:
		case BinaryCSONDataType.TYPE_STRING_MIDDLE:
		case BinaryCSONDataType.TYPE_STRING_LONG:
			return readString(type,rawtype,byteBuffer);
		default:
			return readBuffer(type,rawtype,byteBuffer);
		}
	}
	
	
	public final static String readString(byte type,byte rawtype, ByteBuffer byteBuffer) {
		byte rawType = (byte)(type & 0xF0);
		switch(rawType) {
			case BinaryCSONDataType.TYPE_STRING_SHORT :
				byte shortLen = (byte)(type & 0x0F);
				byte[] shortBuffer = new byte[shortLen];
				byteBuffer.get(shortBuffer, 0, shortLen);
				return new String(shortBuffer, 0, shortBuffer.length, UTF8);
			case BinaryCSONDataType.TYPE_STRING_MIDDLE :
				int middleLenFirst = ((int)(type & 0x0F) << 8); 
				byte middleLenSecond = byteBuffer.get();
				int sizeOfMiddle = (middleLenFirst | (int)(middleLenSecond & 0xFF));
				byte[] middleBuffer = new byte[sizeOfMiddle];
				byteBuffer.get(middleBuffer, 0, sizeOfMiddle);
				return new String(middleBuffer, 0, middleBuffer.length, UTF8);
			case BinaryCSONDataType.TYPE_STRING_LONG :
				int longLenFirst = ((int)(type & 0x0F) << 16); 
				byte longLenSecond = byteBuffer.get();
				byte longLenThird = byteBuffer.get();
				int sizeOfLong = longLenFirst | ((int)(longLenSecond & 0xff) << 8) | (int)(longLenThird & 0xff);
				byte[] longBuffer = new byte[sizeOfLong];
				byteBuffer.get(longBuffer, 0, sizeOfLong);
				return new String(longBuffer, 0, longBuffer.length, UTF8);
		}
		return null;
	}
	
	
	public final static byte[] readBuffer(byte type,byte rawType, ByteBuffer byteBuffer) {
		switch(rawType) {
			case BinaryCSONDataType.TYPE_RAW_MIDDLE :
				int middleLenFirst = ((int)(type & 0x0F) << 8); 
				byte middleLenSecond = byteBuffer.get();
				int sizeOfMiddle = (middleLenFirst | (int)(middleLenSecond & 0xFF));
				byte[] middleBuffer = new byte[sizeOfMiddle];
				byteBuffer.get(middleBuffer, 0, sizeOfMiddle);
				return middleBuffer;
			case BinaryCSONDataType.TYPE_RAW_LONG :
				int longLenFirst = ((int)(type & 0x0F) << 16); 
				byte longLenSecond = byteBuffer.get();
				byte longLenThird = byteBuffer.get();
				int sizeOfLong = longLenFirst | ((int)(longLenSecond & 0xff) << 8) | (int)(longLenThird & 0xff);
				byte[] longBuffer = new byte[sizeOfLong];
				byteBuffer.get(longBuffer, 0, sizeOfLong);
				return longBuffer;
			case BinaryCSONDataType.TYPE_RAW_WILD :
				int wildSize = byteBuffer.getInt();
				byte[] wildBuffer = new byte[wildSize];
				byteBuffer.get(wildBuffer, 0, wildSize);
				return wildBuffer;
		}
		return null;
	}
	
	
	
	public interface ParseCallback {
		public void onVersion(byte[] version);
		public void onOpenObject();
		public void onCloseObject();
		public void onOpenArray();
		public void onCloseArray();
		public void onKey(String key);
		public void onValue(Object value);
	}

}
