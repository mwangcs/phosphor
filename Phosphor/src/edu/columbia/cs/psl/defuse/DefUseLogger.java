package edu.columbia.cs.psl.defuse;

import java.util.concurrent.atomic.AtomicInteger;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.TaintedDouble;
import edu.columbia.cs.psl.phosphor.struct.TaintedDoubleWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedFloat;
import edu.columbia.cs.psl.phosphor.struct.TaintedFloatWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedInt;
import edu.columbia.cs.psl.phosphor.struct.TaintedIntWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedLong;
import edu.columbia.cs.psl.phosphor.struct.TaintedLongWithObjTag;

public class DefUseLogger {

	public static int logDef(int varBeingDefined, String className, String methodName, int byteCodeOffset)
	{
		return varBeingDefined;
	}

	static AtomicInteger tagCounter = new AtomicInteger();
	public static TaintedInt logDef$$PHOSPHORTAGGED(int varBeingDefinedTag, int varBeingDefined, String className, String methodName, int bytecode_offset_tag, int byteCodeOffset, TaintedInt ret)
	{
		//System.out.println("varBeingDeffedTag: "+ varBeingDefinedTag + " varBeingDeffed: "+varBeingDeffed + " className: " + className + " bytecodeoffset_tag: "+bytecode_offset_tag + "byteCodeOffset: " + byteCodeOffset);
		//We can set ret.taint to whatever, and it will be propogated
		ret.taint = tagCounter.incrementAndGet();
		ret.val= varBeingDefined;
		System.out.println("Log def of " + varBeingDefined +  " in " + className+"."+methodName+":"+byteCodeOffset+" - assigning " + ret.taint);
		return ret;
	}

	public static TaintedDouble logDef$$PHOSPHORTAGGED(int varBeingDefinedTag, double varBeingDefined, String className, String methodName, int bytecode_offset_tag, int byteCodeOffset, TaintedDouble ret)
	{
		ret.taint = tagCounter.incrementAndGet();
		ret.val= varBeingDefined;
		System.out.println("Log def of " + varBeingDefined +  " in " + className+"."+methodName+":"+byteCodeOffset+" - assigning " + ret.taint);
		return ret;
	}

	public static TaintedFloat logDef$$PHOSPHORTAGGED(int varBeingDefinedTag, float varBeingDefined, String className, String methodName, int bytecode_offset_tag, int byteCodeOffset, TaintedFloat ret)
	{
		ret.taint = tagCounter.incrementAndGet();
		ret.val= varBeingDefined;
		System.out.println("Log def of " + varBeingDefined +  " in " + className+"."+methodName+":"+byteCodeOffset+" - assigning " + ret.taint);
		return ret;
	}

	public static TaintedLong logDef$$PHOSPHORTAGGED(int varBeingDefinedTag, long varBeingDefined, String className, String methodName, int bytecode_offset_tag, int byteCodeOffset, TaintedLong ret)
	{
		ret.taint = tagCounter.incrementAndGet();
		ret.val= varBeingDefined;
		System.out.println("Log def of " + varBeingDefined +  " in " + className+"."+methodName+":"+byteCodeOffset+" - assigning " + ret.taint);
		return ret;
	}

	public static int logUse(int varBeingUsed, String className, String methodName, int bytecodeoffset)
	{
		return varBeingUsed;
	}

	public static TaintedIntWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, int varBeingUsed, String className, String methodName, int bytecodeoffset_tag, int bytecodeoffset, TaintedInt ret)
	{
		ret.val= varBeingUsed;
		System.out.println("Usage of var, tag is: " + varBeingUsed_tag + ", at " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}

	public static TaintedDoubleWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, double varBeingUsed, String className, String methodName, int bytecodeoffset_tag, int bytecodeoffset, TaintedDouble ret)
	{
		ret.val= varBeingUsed;
		System.out.println("Usage of var, tag is: " + varBeingUsed_tag + ", at " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}

	public static TaintedFloatWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, float varBeingUsed, String className, String methodName, int bytecodeoffset_tag, int bytecodeoffset, TaintedFloat ret)
	{
		ret.val= varBeingUsed;
		System.out.println("Usage of var, tag is: " + varBeingUsed_tag + ", at " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}

	public static TaintedLongWithObjTag logUse$$PHOSPHORTAGGED(Taint varBeingUsed_tag, long varBeingUsed, String className, String methodName, int bytecodeoffset_tag, int bytecodeoffset, TaintedLong ret)
	{
		ret.val= varBeingUsed;
		System.out.println("Usage of var, tag is: " + varBeingUsed_tag + ", at " + className+"."+methodName +":"+bytecodeoffset);
		return ret;

	}
	void example()
	{
		//what we have:
		int i = 0;

		//goal:
		int j = logDef(0,"DefUseLogger.java","example",132);

		//Phosphor turns the above code into:
		TaintedIntWithObjTag _k = logDef$$PHOSPHORTAGGED(0,0,"DefuseLogger.java","example",0,22, new TaintedIntWithObjTag());
		int k = _k.val;
		int k_tag = _k.taint;
	}

}
