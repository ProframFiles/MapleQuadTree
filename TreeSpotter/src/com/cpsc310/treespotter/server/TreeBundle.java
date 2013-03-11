/**
 * 
 */
package com.cpsc310.treespotter.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;

/**
 * @author maple-quadtree
 *
 */
@Entity
public class TreeBundle implements LatLongProvider{

	@Id String bundleID;
	@Ignore LatLong latLong;
	@Ignore ArrayList<TreeData2> trees;
	@Load private Ref<ByteArrayEntity<byte[]>> serializedArrayRef;
	
	
	@Override
	public LatLong getLatLong() {
		if (latLong != null &&trees !=null && !trees.isEmpty()){
			return new LatLong(latLong.getLatitude()/trees.size(), latLong.getLongitude()/trees.size());
		}
		return null;
	}
	
	void putTree(TreeData2 tree){
		latLong.setLatitude(latLong.getLatitude() + tree.getLatLong().getLatitude());
		latLong.setLongitude(latLong.getLongitude() + tree.getLatLong().getLongitude());
		trees.add(tree);
	}
	
	@OnSave
	private void serializeArray(){
		Kryo kryo = new Kryo();
		ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
		Output output = new Output(byte_stream);
		kryo.writeObject(output, trees);
		output.close();
		ByteArrayEntity<byte[]> blob = new ByteArrayEntity<byte[]>();
		blob.setKey(bundleID);
		blob.setBytes(byte_stream.toByteArray());
	}
	
	@SuppressWarnings("unchecked")
	@OnLoad
	public void populateArray(){
		Kryo kryo = new Kryo();
		ByteArrayEntity<byte[]> blob = serializedArrayRef.getValue();
		byte[] b = blob.getBytes();
		Input input = new Input(new ByteArrayInputStream(b));
		trees = kryo.readObject(input, ArrayList.class);
		input.close();
	}
	
	
}
