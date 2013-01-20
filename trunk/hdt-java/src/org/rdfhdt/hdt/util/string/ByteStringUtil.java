/**
 * File: $HeadURL$
 * Revision: $Rev$
 * Last modified: $Date$
 * Last modified by: $Author$
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 *   Alejandro Andres:          fuzzy.alej@gmail.com
 */

package org.rdfhdt.hdt.util.string;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.rdfhdt.hdt.exceptions.NotImplementedException;

/**
 * @author mario.arias
 *
 */
public class ByteStringUtil {
	
	/**
	 * For use in the project when using String.getBytes() and making Strings from byte[]
	 */
	public static final Charset STRING_ENCODING = Charset.forName("UTF-8");
	
	public static int longestCommonPrefix(byte [] buf1, int off1, byte [] buf2, int off2) {
		int len1 = buf1.length;
		int len2 = buf2.length;
		int p1 = off1;
		int p2 = off2;
		while(
				(p1<len1) &&
        	    (p2<len2) &&
        	    (buf1[p1]!=0) &&
        	    (buf2[p2]!=0) && 
        	    (buf1[p1] == buf2[p2])
        	 ){
			p1++;
			p2++;
		}
		return p1-off1;
	} 
	
	public static int longestCommonPrefix(CharSequence str1, CharSequence str2) {
		return longestCommonPrefix(str1, str2, 0);
	}
	
	public static int longestCommonPrefix(CharSequence str1, CharSequence str2, int from) {
		int len = Math.min(str1.length(), str2.length());
		int delta = from;
		while(delta<len && str1.charAt(delta)==str2.charAt(delta)) {
			delta++;
		}
		return delta-from;
	}
	
	public static int strcmp(byte [] buff1, int off1, byte [] buff2, int off2) {
		int len = Math.min(buff1.length-off1, buff2.length-off2);
		
		if(len==0) {
			// Empty string is smaller than any string.
			return (buff2.length-off2)-(buff1.length-off1);
		}
		return strcmp(buff1, off1, buff2, off2, len);
	}
	
	public static int strcmp(byte [] buff1, int off1, byte [] buff2, int off2, int n) {
		byte a,b;
		int diff;
		int p1 = off1;
		int p2 = off2;	
	
		if (n == 0) {
			return 0;
		}
		
		// Check whether one of the buffers is already at the end
		if(p1 < buff1.length && p2==buff2.length) {
			return 1;
		} else if(p1==buff1.length && p2<buff2.length) {
			return -1;
		}
		
		do {
			a = buff1[p1++];
			b = buff2[p2++];
			diff = a-b;
		} while ( (diff==0) && (a!=0) && (--n != 0) );
		
		if(n==0 && diff==0) {
			if(buff1.length-p1!=0 && buff1[p1]!=0){
				// Still remaining in string one, second is shorter
				return 1;
			} 
			if(buff2.length-p2!=0 && buff2[p2]!=0){
				// Still remaining in string two, first is shorter.
				return -1;
			}
			return 0;
		}
		
		return diff;
	}
	
	public static int strlen(byte [] buff, int off) {
		int len = buff.length;
		int pos = off;
		while(pos<len && buff[pos]!=0) {
			pos++;
		}
		return pos-off;
	}
	
	public static int strlen(ByteBuffer pos) {
		pos.mark();
		int len=0;
		while(pos.hasRemaining() && pos.get() != 0) {
			len++;
		}
		pos.reset();
		return len;
	}
	
	public static int strcmp(CharSequence str, byte [] text, int offset) {
		if(str instanceof CompactString) {
			return strcmp(((CompactString) str).getData(), 0, text, offset);
		}
		if(str instanceof String) {
			return strcmp(((String) str).getBytes(ByteStringUtil.STRING_ENCODING), 0, text, offset);
		}
		if(str instanceof ReplazableString) {
			return strcmp(((ReplazableString) str).buffer, 0, text, offset, ((ReplazableString) str).used);
		}
		throw new NotImplementedException();
	}

	public static int strcmp(CharSequence str, ByteBuffer text, int offset) {
		
		text.position(offset);
		
		ByteBuffer a = null;
	
		if(str instanceof CompactString) {
			a = ByteBuffer.wrap(((CompactString) str).getData());
		} else if(str instanceof String) {
			a = ByteBuffer.wrap(((String) str).getBytes(ByteStringUtil.STRING_ENCODING));
		} else if(str instanceof ReplazableString) {
			a = ByteBuffer.wrap(((ReplazableString) str).buffer, 0, ((ReplazableString) str).used);
		} else{
			throw new NotImplementedException();
		}

		return strcmp(a, text);
	}
	
	public static String asString(byte [] buff, int offset) {
		int len = strlen(buff, offset);
		return new String(buff, offset, len);
	}
	
	public static String asString(ByteBuffer buff) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while(buff.hasRemaining()) {
			byte b = buff.get();
			if(b==0) {
				break;
			}
			out.write(b);
		}
		return new String(out.toByteArray());
	}
	
	public static int strcmp(CharSequence str, ByteBuffer buffer) {
		String a = str.toString();
		String b = asString(buffer);
		return a.compareTo(b);
		
		// FIXME: Do the comparison directly to avoid copying, probably faster.
//		byte [] buf=null;
//		
//		if(str instanceof CompactString) {
//			buf = ((CompactString) str).getData();
//		}else if(str instanceof String) {
//			buf = ((String) str).getBytes(ByteStringUtil.STRING_ENCODING);
//		} else {
//			throw new NotImplementedException();
//		}
//		
//		return strcmp(ByteBuffer.wrap(buf), buffer);
	}
	
	public static final int strcmp(ByteBuffer a, ByteBuffer b) {
		// FIXME: Untested. May not work.
	    int n = a.position() + Math.min(a.remaining(), a.remaining());
	    for (int i = a.position(), j = b.position(); i < n; i++, j++) {
	        byte v1 = a.get(i);
	        byte v2 = b.get(j);
	        if(v1==0) {
	        	if(v2==0) {
	        		return 0;
	        	} else {
	        		return 1;
	        	}
	        }
	        if(v2==0) {
	        	if(v1==0) {
	        		return 0;
	        	} else {
	        		return -1;
	        	}
	        }
	        if (v1 == v2)
	            continue;
	        if (v1 < v2)
	            return -1;
	        return +1;
	    }
	    return a.remaining() - b.remaining();
	}
	
	public static int append(CharSequence str, int start, byte [] buffer, int bufpos) {
		byte [] bytes;
		int len;
		if(str instanceof String) {
			bytes = ((String) str).getBytes(ByteStringUtil.STRING_ENCODING);
			len = bytes.length;
		} else if(str instanceof CompactString) {
			bytes = ((CompactString) str).getData();
			len = bytes.length;
		} else if(str instanceof ReplazableString) {
			bytes = ((ReplazableString) str).getBuffer();
			len = ((ReplazableString) str).used;
		} else {
			throw new NotImplementedException();
		}
		System.arraycopy(bytes, start, buffer, bufpos, len - start);
		return len - start;		
	}

	public static int append(OutputStream out, CharSequence str, int start) throws IOException {
		byte [] bytes;
		int len;
		if(str instanceof String) {
			bytes = ((String) str).getBytes(ByteStringUtil.STRING_ENCODING);
			len = bytes.length;
		} else if(str instanceof CompactString) {
			bytes = ((CompactString) str).getData();
			len = bytes.length;
		} else if(str instanceof ReplazableString) {
			bytes = ((ReplazableString) str).getBuffer();
			len = ((ReplazableString) str).used;
		} else {
			throw new NotImplementedException();
		}
		out.write(bytes, start, len - start);
		return len - start;
	}
	
	/**
	 * Add numbits of value to buffer at pos
	 * @param buffer
	 * @param pos Position in bits.
	 * @param value Value to be added
	 * @param numbits numbits of value to be added.
	 */
	public static void append(long [] buffer, long pos, long value, int numbits) {
		final int W = 64;
		
		int i=(int)(pos/W), j=(int)(pos%W);
		
		// NOTE: Assumes it was empty before. 
		// Otherwise we would need to clean the bits beforehand
		if(numbits>(W-j)) {
			// Two words
			buffer[i] |= value << j >>> j;
			buffer[i+1] |= (value<<(W-j-numbits));
		} else {
			buffer[i] |= (value<<(W-j-numbits));
		}
	}
}
