/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2003 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba S.A.                           *
 *      This software is redistributed under the Xiph.org variant of          *
 *      the BSD license.                                                      *
 *      Redistribution and use in source and binary forms, with or without    *
 *      modification, are permitted provided that the following conditions    *
 *      are met:                                                              *
 *      - Redistributions of source code must retain the above copyright      *
 *      notice, this list of conditions and the following disclaimer.         *
 *      - Redistributions in binary form must reproduce the above copyright   *
 *      notice, this list of conditions and the following disclaimer in the   *
 *      documentation and/or other materials provided with the distribution.  *
 *      - Neither the name of Wimba, the Xiph.org Foundation nor the names of *
 *      its contributors may be used to endorse or promote products derived   *
 *      from this software without specific prior written permission.         *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba S.A. is not liable for any consequence related to the           *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: Bits.java                                                           *
 *                                                                            *
 * Author: James LAWRENCE                                                     *
 * Modified by: Marc GIMPEL                                                   *
 * Based on code by: Jean-Marc VALIN                                          *
 *                                                                            *
 * Date: March 2003                                                           *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

/* Copyright (C) 2002 Jean-Marc Valin 

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:
   
   - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   
   - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   
   - Neither the name of the Xiph.org Foundation nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.xiph.speex;

/**
 * Speex bit packing and unpacking class
 */
public class Bits
{
  /**< "raw" data */
  private byte bytes[];
  
  /**< Position of the byte "cursor" */
  private int  bytePtr;
  
  /**< Position of the bit "cursor" within the current byte */
  private int  bitPtr;  
  
  /**
   * Initialise
   */
  public void init()
  {
    bytes = new byte[2000];
    bytePtr=0;
    bitPtr=0;
  }

  /**
   * Advance n bits
   */
  public void advance(int n)
  {
    int nbytes= n >> 3, nbits= n & 7;
    bytePtr += nbytes;
    bitPtr += nbits;
    
    if (bitPtr>7) {
      bitPtr-=8;
      bytePtr++;
    }
  }

  /**
   * Sets the buffer to the given value
   */
  protected void setBuffer(byte[] pBuffer){
    bytes = pBuffer;
  }  
  
  /**
   * Take a peek at the next bit
   */
  public int peek()
  {
    return ((bytes[bytePtr] & 0xFF)>>(7-bitPtr))&1;
  }

  /**
   * Read the given array into the buffer
   */
  public void read_from(byte newbytes[], int offset, int len)
  {
    for (int i=0;i<len;i++)
      bytes[i]=newbytes[offset+i];
    bytePtr=0;
    bitPtr=0;
  }

  /**
   * Read the next N bits from the buffer.
   */
  public int unpack(int nbBits)  
  {
    int d=0;
    while(nbBits!=0)
    {
      d<<=1;
      d |= ((bytes[bytePtr] & 0xFF)>>(7-bitPtr))&1;
      bitPtr++;
      if (bitPtr==8)
      {
        bitPtr=0;
        bytePtr++;
      }
      nbBits--;
    }
    return d;
  }
  
  /**
   * Write n bits of the given data to the buffer
   */
  public void pack(int data, int nbBits)
  {
    int i;
    int d=data;

    while(bytePtr+((nbBits+bitPtr)>>3) >= bytes.length)
    {
      System.err.println("Buffer too small to pack bits");
      int size = bytes.length*2;
      byte[] tmp = new byte[size];
      System.arraycopy(bytes, 0, tmp, 0, bytes.length);
      bytes = tmp;
    }
    while(nbBits>0)
    {
      int bit;
      bit = (d>>(nbBits-1))&1;
      bytes[bytePtr] |= bit<<(7-bitPtr);
      bitPtr++;
      if (bitPtr==8)
      {
        bitPtr=0;
        bytePtr++;
      }
      nbBits--;
    }
  }
  
  /**
   * Returns the current buffer array
   */
  public byte[] getBuffer()
  {
    return bytes;
  }

  /**
   * Returns the number of bytes used in the current buffer 
   */
  public int getBufferSize()
  {
    return bytePtr + (bitPtr > 0 ? 1 : 0);
  }
}
