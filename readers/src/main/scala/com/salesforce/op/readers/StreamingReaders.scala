/*
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.op.readers

import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.fs.Path

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.WeakTypeTag


/**
 * Just a handy factory for streaming readers
 */
object StreamingReaders {

  /**
   * Simple streaming reader factory
   */
  object Simple {

    private[readers] def defaultPathFiler(p: Path) = Seq(".", "_").forall(!p.getName.startsWith(_))

    /**
     * Creates [[FileStreamingAvroReader]]
     *
     * @param key          function for extracting key from avro record
     * @param filter       Function to filter paths to process
     * @param newFilesOnly Should process only new files and ignore existing files in the directory
     */
    def avro[T <: GenericRecord : ClassTag : WeakTypeTag](
      key: T => String = ReaderKey.randomKey _,
      filter: Path => Boolean = defaultPathFiler,
      newFilesOnly: Boolean = false
    ): FileStreamingAvroReader[T] = new FileStreamingAvroReader[T](key, filter, newFilesOnly)

  }

}
