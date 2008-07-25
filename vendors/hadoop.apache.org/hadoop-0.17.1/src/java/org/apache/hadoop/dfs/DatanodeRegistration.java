/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.dfs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableFactories;
import org.apache.hadoop.io.WritableFactory;

/** 
 * DatanodeRegistration class conatins all information the Namenode needs
 * to identify and verify a Datanode when it contacts the Namenode.
 * This information is sent by Datanode with each communication request.
 * 
 */
class DatanodeRegistration extends DatanodeID implements Writable {
  static {                                      // register a ctor
    WritableFactories.setFactory
      (DatanodeRegistration.class,
       new WritableFactory() {
         public Writable newInstance() { return new DatanodeRegistration(); }
       });
  }

  StorageInfo storageInfo;

  /**
   * Default constructor.
   */
  public DatanodeRegistration() {
    super(null, null, -1);
    this.storageInfo = new StorageInfo();
  }
  
  /**
   * Create DatanodeRegistration
   */
  public DatanodeRegistration(String nodeName) {
    super(nodeName, "", -1);
    this.storageInfo = new StorageInfo();
  }
  
  void setInfoPort(int infoPort) {
    this.infoPort = infoPort;
  }
  
  void setStorageInfo(DataStorage storage) {
    this.storageInfo = new StorageInfo(storage);
    this.storageID = storage.getStorageID();
  }
  
  void setName(String name) {
    this.name = name;
  }

  /**
   */
  public int getVersion() {
    return storageInfo.getLayoutVersion();
  }
  
  /**
   */
  public String getRegistrationID() {
    return Storage.getRegistrationID(storageInfo);
  }

  /////////////////////////////////////////////////
  // Writable
  /////////////////////////////////////////////////
  /**
   */
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeInt(storageInfo.getLayoutVersion());
    out.writeInt(storageInfo.getNamespaceID());
    out.writeLong(storageInfo.getCTime());
  }

  /**
   */
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    storageInfo.layoutVersion = in.readInt();
    storageInfo.namespaceID = in.readInt();
    storageInfo.cTime = in.readLong();
  }
}