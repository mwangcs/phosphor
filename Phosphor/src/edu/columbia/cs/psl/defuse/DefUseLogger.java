package edu.columbia.cs.psl.defuse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.TaintedDoubleWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedFloatWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedIntWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedLongWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedWithObjTag;

public class DefUseLogger {

	private static Map<String, Integer> hm = new ConcurrentHashMap<String, Integer>();

	public static int logDef(int varBeingDefined, String className, String methodName, int byteCodeOffset)
	{
		return varBeingDefined;
	}

	private static String generateLabel(String className, String methodName, int bytecodeOffset)
	{
		String id = className+"."+methodName+":"+bytecodeOffset;
		if(hm.containsKey(id)){
			hm.put(id, hm.get(id)+1);
		}
		else{
			hm.put(id, 0);
		}
		return id+"("+hm.get(id)+")";
	}
	public static void logDef$$PHOSPHORTAGGED(Object obj, String className, String methodName, Taint bytecodeOffsetTaint, int bytecodeOffset)
	{
		if(obj instanceof TaintedWithObjTag)
		{
			((TaintedWithObjTag) obj).setPHOSPHOR_TAG(new Taint(null));
		}
	}
	public static void logUse$$PHOSPHORTAGGED(Object obj, String className, String methodName,Taint bytecodeOffsetTaint, int bytecodeOffset)
	{
		if(obj instanceof TaintedWithObjTag)
		{
			((TaintedWithObjTag) obj).getPHOSPHOR_TAG();
		}
	}
	public static TaintedIntWithObjTag logDef$$PHOSPHORTAGGED(Taint varBeingDefinedTag, int varBeingDefined, String className, String methodName, Taint bytecode_offset_tag, int byteCodeOffset, TaintedIntWithObjTag ret)
	{
		//System.out.println("varBeingDeffedTag: "+ varBeingDefinedTag + " varBeingDeffed: "+varBeingDeffed + " className: " + className + " bytecodeoffset_tag: "+bytecode_offset_tag + "byteCodeOffset: " + byteCodeOffset);
		//We can set ret.taint to whatever, and it will be propogated
		ret.taint = new Taint(varBeingDefined+ " " +generateLabel(className, methodName, byteCodeOffset));
		ret.val= varBeingDefined;
		return ret;
	}

	public static TaintedDoubleWithObjTag logDef$$PHOSPHORTAGGED(Taint varBeingDefinedTag, double varBeingDefined, String className, String methodName, Taint bytecode_offset_tag, int byteCodeOffset, TaintedDoubleWithObjTag ret)
	{
		ret.taint = new Taint(varBeingDefined+ " " +generateLabel(className, methodName, byteCodeOffset));
		ret.val= varBeingDefined;
		return ret;
	}

	public static TaintedFloatWithObjTag logDef$$PHOSPHORTAGGED(Taint varBeingDefinedTag, float varBeingDefined, String className, String methodName, Taint bytecode_offset_tag, int byteCodeOffset, TaintedFloatWithObjTag ret)
	{
		ret.taint = new Taint(varBeingDefined+ " " +generateLabel(className, methodName, byteCodeOffset));
		ret.val= varBeingDefined;
		return ret;
	}

	public static TaintedLongWithObjTag logDef$$PHOSPHORTAGGED(Taint varBeingDefinedTag, long varBeingDefined, String className, String methodName, Taint bytecode_offset_tag, int byteCodeOffset, TaintedLongWithObjTag ret)
	{
		ret.taint = new Taint(varBeingDefined+ " " +generateLabel(className, methodName, byteCodeOffset));
		ret.val= varBeingDefined;
		return ret;
	}

	public static int logUse(int varBeingUsed, String className, String methodName, int bytecodeoffset)
	{
		return varBeingUsed;
	}

	public static TaintedIntWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, int varBeingUsed, String className, String methodName, Taint bytecodeoffset_tag, int bytecodeoffset, TaintedIntWithObjTag ret)
	{

		ret.val= varBeingUsed;
		System.out.println(" Def: " + varBeingUsed_tag.lbl.toString() + ", Use: " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}

	public static TaintedDoubleWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, double varBeingUsed, String className, String methodName, Taint bytecodeoffset_tag, int bytecodeoffset, TaintedDoubleWithObjTag ret)
	{
		ret.val= varBeingUsed;
		System.out.println(" Def: " + varBeingUsed_tag.lbl.toString() + ", Use: " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}

	public static TaintedFloatWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, float varBeingUsed, String className, String methodName, Taint bytecodeoffset_tag, int bytecodeoffset, TaintedFloatWithObjTag ret)
	{
		ret.val= varBeingUsed;
		System.out.println(" Def: " + varBeingUsed_tag.lbl.toString() + ", Use: " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}

	public static TaintedLongWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, long varBeingUsed, String className, String methodName, Taint bytecodeoffset_tag, int bytecodeoffset, TaintedLongWithObjTag ret)
	{
		ret.val= varBeingUsed;
		System.out.println(" Def: " + varBeingUsed_tag.lbl.toString() + ", Use: " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}
	void example()
	{
		//what we have:
		int i = 0;

		//goal:
		int j = logDef(0,"DefUseLogger.java","example",132);

		//Phosphor turns the above code into:
		//TaintedIntWithObjTag _k = logDef$$PHOSPHORTAGGED(0,0,"DefuseLogger.java","example",0,22, new TaintedIntWithObjTag());
		//int k = _k.val;
		//int k_tag = _k.taint;
	}

}
