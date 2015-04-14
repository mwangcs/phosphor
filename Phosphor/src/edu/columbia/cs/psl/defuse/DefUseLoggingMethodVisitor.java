package edu.columbia.cs.psl.defuse;


import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.util.Printer;
import edu.columbia.cs.psl.phosphor.struct.TaintedDouble;
import edu.columbia.cs.psl.phosphor.struct.TaintedInt;



public class DefUseLoggingMethodVisitor extends MethodVisitor{


	private String className;
	private String methodName;
	public DefUseLoggingMethodVisitor(MethodVisitor mv, String className, String methodName) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.methodName = methodName;
	}
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		//System.out.println("Visit " + Printer.OPCODES[opcode] +  name + " - " + desc);
		Type fieldType = Type.getType(desc);
		switch(fieldType.getSort())
		{
		case Type.OBJECT:
		case Type.ARRAY:
			break;
		case Type.INT:
		case Type.BOOLEAN:
		case Type.BYTE:
		case Type.CHAR:
		case Type.DOUBLE:
		case Type.FLOAT:
		case Type.LONG:
		case Type.SHORT:
			if(opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD)
			{
				//Variable being stored is at this location on stack
				super.visitLdcInsn(className);
				super.visitLdcInsn(methodName);
				super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
				super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

				super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logDef", "("+desc+"Ljava/lang/String;Ljava/lang/String;I)"+desc, false);
			}
			else if(opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD)
			{
				//Loads variable stored in "var" onto stack
				super.visitFieldInsn(opcode, owner, name, desc);
				super.visitLdcInsn(className);
				super.visitLdcInsn(methodName);
				super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
				super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

				super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logUse", "("+desc+"Ljava/lang/String;Ljava/lang/String;I)"+desc, false);
				return;
			}
			break;
			default:
				throw new UnsupportedOperationException();
		}

		super.visitFieldInsn(opcode, owner, name, desc);
	}
	int currentLineNumber;
	@Override
	public void visitLineNumber(int line, Label start) {
		// TODO Auto-generated method stub
		super.visitLineNumber(line, start);
		currentLineNumber = line;
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		super.visitIincInsn(var, increment);
	}
	public void visitVarInsn(int opcode, int var)
	{
		if(opcode == Opcodes.ISTORE)
		{
			//Variable being stored is at this location on stack
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logDef", "(ILjava/lang/String;Ljava/lang/String;I)I", false);
		}
		else if(opcode == Opcodes.ILOAD)
		{
			//Loads variable stored in "var" onto stack
			super.visitVarInsn(opcode, var);
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logUse", "(ILjava/lang/String;Ljava/lang/String;I)I", false);
			return;
		}
		else if(opcode == Opcodes.DSTORE){
			//Variable being stored is at this location on stack
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logDef", "(DLjava/lang/String;Ljava/lang/String;I)D", false);

		}
		else if(opcode == Opcodes.DLOAD)
		{
			//Loads variable stored in "var" onto stack
			super.visitVarInsn(opcode, var);
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logUse", "(DLjava/lang/String;Ljava/lang/String;I)D", false);
			return;
		}
		else if(opcode == Opcodes.FSTORE){
			//Variable being stored is at this location on stack
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logDef", "(FLjava/lang/String;Ljava/lang/String;I)F", false);
		}
		else if(opcode == Opcodes.FLOAD)
		{
			//Loads variable stored in "var" onto stack
			super.visitVarInsn(opcode, var);
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logUse", "(FLjava/lang/String;Ljava/lang/String;I)F", false);
			return;
		}
		else if(opcode == Opcodes.LSTORE){
			//Variable being stored is at this location on stack
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logDef", "(JLjava/lang/String;Ljava/lang/String;I)J", false);
		}
		else if(opcode == Opcodes.LLOAD)
		{
			//Loads variable stored in "var" onto stack
			super.visitVarInsn(opcode, var);
			super.visitLdcInsn(className);
			super.visitLdcInsn(methodName);
			super.visitInsn(Opcodes.ICONST_0); //Taint tag for line number
			super.visitIntInsn(Opcodes.BIPUSH,currentLineNumber); //Line number

			super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DefUseLogger.class), "logUse", "(JLjava/lang/String;Ljava/lang/String;I)J", false);
			return;
		}


		super.visitVarInsn(opcode, var);
		//System.out.println("Visit instruction: "+Printer.OPCODES[opcode] + " " + var);
	}
	@Override
	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
		//		System.out.println("Visit instruction: "+Printer.OPCODES[opcode]);
	}

	@Override
	public void visitLdcInsn(Object cst){
		super.visitLdcInsn(cst);
	}
}