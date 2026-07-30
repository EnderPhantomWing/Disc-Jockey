/*
 * MIT License
 *
 * Copyright (c) 2022 SemmieDev
 * Copyright (c) 2023 EnderKill98
 * Copyright (c) 2025 myueqf
 * Copyright (c) 2026 EnderPhantomWing
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package semmiedev.disc_jockey.disc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryReader {
    private final InputStream in;
    private final ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

    public BinaryReader(InputStream in) {
        this.in = in;
    }

    public int readInt() throws IOException {
        return buffer.clear().put(readBytes(Integer.BYTES)).rewind().getInt();
    }

    public long readUInt() throws IOException {
        return readInt() & 0xFFFFFFFFL;
    }

    public int readUShort() throws IOException {
        return readShort() & 0xFFFF;
    }

    public short readShort() throws IOException {
        return buffer.clear().put(readBytes(Short.BYTES)).rewind().getShort();
    }

    public String readString() throws IOException {
        return new String(readBytes(readInt()));
    }

    public float readFloat() throws IOException {
        return buffer.clear().put(readBytes(Float.BYTES)).rewind().getFloat();
    }

    /*private int getStringLength() throws IOException {
        int count = 0;
        int shift = 0;
        boolean more = true;
        while (more) {
            byte b = (byte) in.read();
            count |= (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                more = false;
            }
        }
        return count;
    }*/

    public byte readByte() throws IOException {
        int b = in.read();
        if (b < 0) throw new EOFException();
        return (byte)(b);
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) bytes[i] = readByte();
        return bytes;
    }
}
