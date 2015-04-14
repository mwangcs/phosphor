package edu.columbia.cs.psl.phosphor.instrumenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.ISTORE;

import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.BasicArrayInterpreter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.FrameNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LocalVariableNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.VarInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Analyzer;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.BasicValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.TaintedDouble;
import edu.columbia.cs.psl.phosphor.struct.TaintedFloat;
import edu.columbia.cs.psl.phosphor.struct.TaintedInt;
import edu.columbia.cs.psl.phosphor.struct.TaintedLong;

public class PrimitiveArrayAnalyzer extends MethodVisitor {
	final class PrimitiveArrayAnalyzerMN extends MethodNode {
		private final String className;
		private final MethodVisitor cmv;
		boolean[] endsWithGOTO;
		int curLabel = 0;
		HashMap<Integer, Boolean> lvsThatAreArrays = new HashMap<Integer, Boolean>();
		ArrayList<FrameNode> inFrames = new ArrayList<FrameNode>();
		ArrayList<FrameNode> outFrames = new ArrayList<FrameNode>();

		public PrimitiveArrayAnalyzerMN(int access, String name, String desc, String signature, String[] exceptions, String className, MethodVisitor cmv) {
			super(Opcodes.ASM5,access, name, desc, signature, exceptions);
			this.className = className;
			this.cmv = cmv;
		}

		@Override
		public void visitCode() {
			if (DEBUG)
				System.out.println("Visiting: " + className + "." + name + desc);
			Label firstLabel = new Label();
			super.visitCode();
			visitLabel(firstLabel);

		}

		//			@Override
		//			public void visitVarInsn(int opcode, int var) {
		//				if(opcode == Opcodes.ASTORE)
		//				{
		//					boolean isPrimArray = TaintAdapter.isPrimitiveStackType(analyzer.stack.get(analyzer.stack.size() - 1));
		//					if(lvsThatAreArrays.containsKey(var))
		//					{
		//						if(lvsThatAreArrays.get(var) != isPrimArray)
		//						{
		//							throw new IllegalStateException("This analysis is currently too lazy to handle when you have 1 var slot take different kinds of arrays");
		//						}
		//					}
		//					lvsThatAreArrays.put(var, isPrimArray);
		//				}
		//				super.visitVarInsn(opcode, var);
		//			}
		private  void visitFrameTypes(final int n, final Object[] types,
				final List<Object> result) {
			for (int i = 0; i < n; ++i) {
				Object type = types[i];
				result.add(type);
				if (type == Opcodes.LONG || type == Opcodes.DOUBLE) {
					result.add(Opcodes.TOP);
				}
			}
		}

		FrameNode generateFrameNode(int type, int nLocal, Object[] local, int nStack, Object[] stack)
		{
			FrameNode ret = new FrameNode(type, nLocal, local, nStack, stack);
			ret.local = new ArrayList<Object>();
			ret.stack= new ArrayList<Object>();
			visitFrameTypes(nLocal, local, ret.local);
			visitFrameTypes(nStack, stack, ret.stack);
			return ret;
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			if (DEBUG)
				System.out.println("Visitframe curlabel " + (curLabel - 1));
			super.visitFrame(type, nLocal, local, nStack, stack);
			if (DEBUG)
				System.out.println("label " + (curLabel - 1) + " reset to " + Arrays.toString(stack));
			if (inFrames.size() == curLabel - 1)
				inFrames.add(generateFrameNode(type, nLocal, local, nStack, stack));
			else
				inFrames.set(curLabel - 1, generateFrameNode(type, nLocal, local, nStack, stack));
			//				System.out.println(name+" " +Arrays.toString(local));
			//				if (curLabel > 0) {
			//				System.out.println("And resetting outframe " + (curLabel - 2));
			//					if (outFrames.size() == curLabel - 1)
			//						outFrames.add(new FrameNode(type, nLocal, local, nStack, stack));
			//					 if(outFrames.get(curLabel -1) == null)
			//						outFrames.set(curLabel - 1, new FrameNode(type, nLocal, local, nStack, stack));
			//				}
		}

		@Override
		public void visitLabel(Label label) {
			//				if (curLabel >= 0)
			if (DEBUG)
				System.out.println("Visit label: " + curLabel + " analyzer: " + analyzer.stack + " inframes size " + inFrames.size() + " " + outFrames.size());
			if (analyzer.locals == null || analyzer.stack == null)
				inFrames.add(new FrameNode(0, 0, new Object[0], 0, new Object[0]));
			else
				inFrames.add(new FrameNode(0, analyzer.locals.size(), analyzer.locals.toArray(), analyzer.stack.size(), analyzer.stack.toArray()));
			//				if (outFrames.size() <= curLabel) {
			//					if(analyzer.stack == null)
			outFrames.add(null);
			if (curLabel > 0 && outFrames.get(curLabel - 1) == null && analyzer.stack != null)
				outFrames.set(curLabel - 1, new FrameNode(0, analyzer.locals.size(), analyzer.locals.toArray(), analyzer.stack.size(), analyzer.stack.toArray()));
			if (DEBUG)
				System.out.println("Added outframe for " + (outFrames.size() - 1) + " : " + analyzer.stack);
			//				}

			super.visitLabel(label);
			curLabel++;
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
			if (DEBUG)
				System.out.println("Rewriting " + curLabel + " OUT to " + analyzer.stack);
			outFrames.set(curLabel - 1, new FrameNode(0, analyzer.locals.size(), analyzer.locals.toArray(), analyzer.stack.size(), analyzer.stack.toArray()));
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			if (DEBUG)
				System.out.println("Rewriting " + curLabel + " OUT to " + analyzer.stack);
			outFrames.set(curLabel - 1, new FrameNode(0, analyzer.locals.size(), analyzer.locals.toArray(), analyzer.stack.size(), analyzer.stack.toArray()));
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == Opcodes.ATHROW) {
				if (DEBUG)
					System.out.println("Rewriting " + curLabel + " OUT to " + analyzer.stack);
				outFrames.set(curLabel - 1, new FrameNode(0, analyzer.locals.size(), analyzer.locals.toArray(), analyzer.stack.size(), analyzer.stack.toArray()));
			}
			super.visitInsn(opcode);
		}

		public void visitJumpInsn(int opcode, Label label) {
			//				System.out.println(opcode);
			//				if (opcode == Opcodes.GOTO) {
			super.visitJumpInsn(opcode, label);
			int nToPop = 0;
			switch (opcode) {
			case Opcodes.IFEQ:
			case Opcodes.IFNE:
			case Opcodes.IFLT:
			case Opcodes.IFGE:
			case Opcodes.IFGT:
			case Opcodes.IFLE:
			case Opcodes.IFNULL:
			case Opcodes.IFNONNULL:
				//pop 1
				nToPop = 1;
				break;
			case Opcodes.IF_ICMPEQ:
			case Opcodes.IF_ICMPNE:
			case Opcodes.IF_ICMPLT:
			case Opcodes.IF_ICMPGE:
			case Opcodes.IF_ICMPGT:
			case Opcodes.IF_ICMPLE:
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ACMPNE:
				//pop 2
				nToPop = 2;
				break;
			case Opcodes.GOTO:
				//pop none
				break;
			default:
				throw new IllegalArgumentException();
			}
			//The analyzer won't have executed yet, so simulate it did :'(
			List<Object> stack = new ArrayList<Object>(analyzer.stack);
			//				System.out.println("got to remove " + nToPop +  " from " + analyzer.stack + " in " + className + "."+name );
			while (nToPop > 0 && stack.size() > 0) {
				stack.remove(stack.size() - 1);
				nToPop--;
			}

			if (DEBUG)
				System.out.println(name + " Rewriting " + curLabel + " OUT to " + stack);
			outFrames.set(curLabel - 1, new FrameNode(0, analyzer.locals.size(), analyzer.locals.toArray(), stack.size(), stack.toArray()));
			visitLabel(new Label());
			//				}

		}

		@Override
		public void visitEnd() {
			final HashMap<Integer, LinkedList<Integer>> neverAutoBoxByFrame = new HashMap<Integer, LinkedList<Integer>>();
			final HashMap<Integer, LinkedList<Integer>> alwaysAutoBoxByFrame = new HashMap<Integer, LinkedList<Integer>>();
			final HashMap<Integer, LinkedList<Integer>> outEdges = new HashMap<Integer, LinkedList<Integer>>();
			final HashSet<Integer> insertACHECKCASTBEFORE = new HashSet<Integer>();
			final HashSet<Integer> insertACONSTNULLBEFORE = new HashSet<Integer>();
			Analyzer<BasicValue> a = new Analyzer<BasicValue>(new BasicArrayInterpreter()) {
				int getLabel(int insn) {
					int label = -1;
					for (int j = 0; j <= insn; j++) {
						label = insnToLabel[j];
					}
					return label;
				}

				int getInsnAfterFrameFor(int insn) {
					int r = 0;
					for (int i = 0; i < insn; i++) {
						if (instructions.get(i).getType() == AbstractInsnNode.FRAME)
							r = i + 1;
					}
					return r;
				}

				int getLastInsnByLabel(int label) {
					int r = 0;
					for (int j = 0; j < insnToLabel.length; j++) {
						if (insnToLabel[j] == label) {
							if (instructions.get(j).getType() == AbstractInsnNode.FRAME)
								continue;
							r = j;
						}
					}
					return r;
				}

				int getFirstInsnByLabel(int label) {
					for (int j = 0; j < insnToLabel.length; j++) {
						if (insnToLabel[j] == label) {
							if (instructions.get(j).getType() == AbstractInsnNode.FRAME || instructions.get(j).getType() == AbstractInsnNode.LABEL
									|| instructions.get(j).getType() == AbstractInsnNode.LINE)
								continue;
							return j;
						}
					}
					return -1;
				}

				@Override
				public Frame<BasicValue>[] analyze(String owner, MethodNode m) throws AnalyzerException {
					Iterator<AbstractInsnNode> insns = m.instructions.iterator();
					insnToLabel = new int[m.instructions.size()];
					endsWithGOTO = new boolean[insnToLabel.length];

					//						System.out.println(name);
					int label = -1;
					boolean isFirst = true;
					while (insns.hasNext()) {
						AbstractInsnNode insn = insns.next();
						int idx = m.instructions.indexOf(insn);

						if (insn instanceof LabelNode) {
							label++;
						}

						if (insn.getOpcode() == Opcodes.GOTO) {
							endsWithGOTO[idx] = true;
						}
						insnToLabel[idx] = (isFirst ? 1 : label);
						isFirst = false;
						//														System.out.println(idx + "->"+label);
					}
					Frame<BasicValue>[] ret = super.analyze(owner, m);
					//					if (DEBUG)
					//						for (int i = 0; i < inFrames.size(); i++) {
					//							System.out.println("IN: " + i + " " + inFrames.get(i).stack);
					//						}
					//					if (DEBUG)
					//						for (int i = 0; i < outFrames.size(); i++) {
					//							System.out.println("OUT: " + i + " " + (outFrames.get(i) == null ? "null" : outFrames.get(i).stack));
					//						}

					for (Integer successor : edges.keySet()) {
						if (edges.get(successor).size() > 1) {
							int labelToSuccessor = getLabel(successor);

							if (DEBUG)
								System.out.println(name + " Must merge: " + edges.get(successor) + " into " + successor + " AKA " + labelToSuccessor);
							if (DEBUG)
								System.out.println("Input to successor: " + inFrames.get(labelToSuccessor).stack);

							for (Integer toMerge : edges.get(successor)) {
								int labelToMerge = getLabel(toMerge);
								if (DEBUG)
									System.out.println(toMerge + " AKA " + labelToMerge);
								if (DEBUG)
									System.out.println((outFrames.get(labelToMerge) == null ? "null" : outFrames.get(labelToMerge).stack));
								if (outFrames.get(labelToMerge).stack.size() > 0 && inFrames.get(labelToSuccessor).stack.size() > 0) {
									Object output1Top = outFrames.get(labelToMerge).stack.get(outFrames.get(labelToMerge).stack.size() - 1);
									Object inputTop = inFrames.get(labelToSuccessor).stack.get(inFrames.get(labelToSuccessor).stack.size() - 1);
									if (output1Top == Opcodes.TOP)
										output1Top = outFrames.get(labelToMerge).stack.get(outFrames.get(labelToMerge).stack.size() - 2);
									if (inputTop == Opcodes.TOP)
										inputTop = inFrames.get(labelToSuccessor).stack.get(inFrames.get(labelToSuccessor).stack.size() - 2);
									//									System.out.println(className+"."+name+ " IN"+inputTop +" OUT " + output1Top);
									if (output1Top != null && output1Top != inputTop) {
										Type inputTopType = TaintAdapter.getTypeForStackType(inputTop);
										Type outputTopType = TaintAdapter.getTypeForStackType(output1Top);
										if ((output1Top == Opcodes.NULL) && inputTopType.getSort() == Type.ARRAY && inputTopType.getElementType().getSort() != Type.OBJECT
												&& inputTopType.getDimensions() == 1) {
											insertACONSTNULLBEFORE.add(toMerge);
										} else if ((inputTopType.getSort() == Type.OBJECT || (inputTopType.getSort() == Type.ARRAY && inputTopType.getElementType().getSort() == Type.OBJECT)) && outputTopType.getSort() == Type.ARRAY && outputTopType.getElementType().getSort() != Type.OBJECT
												&& inputTopType.getDimensions() == 1) {
											insertACHECKCASTBEFORE.add(toMerge);
										}
									}
								}
								if (outFrames.get(labelToMerge).local.size() > 0 && inFrames.get(labelToSuccessor).local.size() > 0) {
									for (int i = 0; i < Math.min(outFrames.get(labelToMerge).local.size(), inFrames.get(labelToSuccessor).local.size()); i++) {
										Object out = outFrames.get(labelToMerge).local.get(i);
										Object in = inFrames.get(labelToSuccessor).local.get(i);
										//										System.out.println(name +" " +out + " out, " + in + " In" + " i "+i);
										if (out instanceof String && in instanceof String) {
											Type tout = Type.getObjectType((String) out);
											Type tin = Type.getObjectType((String) in);
											if (tout.getSort() == Type.ARRAY && tout.getElementType().getSort() != Type.OBJECT && tout.getDimensions() == 1 && tin.getSort() == Type.OBJECT) {
												int insnN = getLastInsnByLabel(labelToMerge);
												//												System.out.println(name+desc);
												//																							System.out.println(outFrames + " out, " + in + " In" + " i "+i);
												//												System.out.println("T1::"+tout + " to " + tin + " this may be unsupported but should be handled by the above! in label " + instructions.get(insnN));
												//												System.out.println("In insn is " + getFirstInsnByLabel(labelToSuccessor));
												//												System.out.println("insn after frame is " + insnN +", " + instructions.get(insnN) + "<"+instructions.get(insnN).getOpcode());
												//													System.out.println(inFrames.get(labelToSuccessor).local);
												if (!alwaysAutoBoxByFrame.containsKey(insnN))
													alwaysAutoBoxByFrame.put(insnN, new LinkedList<Integer>());
												alwaysAutoBoxByFrame.get(insnN).add(i);
											}
										}
									}
								}
							}
						}
					}

					//TODO: if the output of a frame is an array but the input is an obj, hint to always box?
					//or is that necessary, because we already assume that it's unboxed.
					return ret;
				}

				HashMap<Integer, LinkedList<Integer>> edges = new HashMap<Integer, LinkedList<Integer>>();


				@Override
				protected void newControlFlowEdge(int insn, int successor) {
					if (!edges.containsKey(successor))
						edges.put(successor, new LinkedList<Integer>());
					if (!edges.get(successor).contains(insn))
						edges.get(successor).add(insn);
					if (!outEdges.containsKey(insn))
						outEdges.put(insn, new LinkedList<Integer>());
					if (!outEdges.get(insn).contains(successor))
						outEdges.get(insn).add(successor);
					super.newControlFlowEdge(insn, successor);
				}
			};
			try {

				Frame<BasicValue>[] frames = a.analyze(className, this);
				//				HashMap<Integer,BasicBlock> cfg = new HashMap<Integer, BasicBlock>();
				//				for(Integer i : outEdges.keySet())
				//				{
				//					BasicBlock b = new BasicBlock();
				//					b.idx = i;
				//					b.outEdges = outEdges.get(i);
				//					int endIdx = this.instructions.size();
				//					for(Integer jj : outEdges.get(i))
				//						if(i < endIdx)
				//							endIdx = jj;
				//					for(int j =i; j < endIdx; j++)
				//					{
				//						if(instructions.get(i) instanceof VarInsnNode)
				//						{
				//							VarInsnNode n = ((VarInsnNode) instructions.get(i));
				//							b.varsAccessed.add(n.var);
				//						}
				//					}
				//					cfg.put(i, b);
				//				}
				//				for(Integer i : cfg.keySet())
				//				{
				//					computeVarsAccessed(i,cfg);
				//				}
				ArrayList<Integer> toAddNullBefore = new ArrayList<Integer>();
				toAddNullBefore.addAll(insertACONSTNULLBEFORE);

				toAddNullBefore.addAll(insertACHECKCASTBEFORE);
				toAddNullBefore.addAll(neverAutoBoxByFrame.keySet());
				toAddNullBefore.addAll(alwaysAutoBoxByFrame.keySet());
				Collections.sort(toAddNullBefore);

				HashMap<LabelNode, LabelNode> problemLabels = new HashMap<LabelNode, LabelNode>();
				HashMap<LabelNode, HashSet<Integer>> problemVars = new HashMap<LabelNode, HashSet<Integer>>();
				int nNewNulls = 0;
				for (Integer i : toAddNullBefore) {
					AbstractInsnNode insertAfter = this.instructions.get(i + nNewNulls);

					if (insertACONSTNULLBEFORE.contains(i)) {
						if (DEBUG)
							System.out.println("Adding Null before: " + i);
						if (insertAfter.getOpcode() == Opcodes.GOTO)
							insertAfter = insertAfter.getPrevious();
						this.instructions.insert(insertAfter, new InsnNode(Opcodes.ACONST_NULL));
						nNewNulls++;
					} else if (insertACHECKCASTBEFORE.contains(i)) {
						if (DEBUG)
							System.out.println("Adding checkcast before: " + i + " (plus " + nNewNulls + ")");
						if (insertAfter.getOpcode() == Opcodes.GOTO)
							insertAfter = insertAfter.getPrevious();
						this.instructions.insert(insertAfter, new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Object.class)));
						nNewNulls++;
					} else if (neverAutoBoxByFrame.containsKey(i)) {
						if (insertAfter.getOpcode() == Opcodes.GOTO)
							insertAfter = insertAfter.getPrevious();
						for (int j : neverAutoBoxByFrame.get(i)) {
							//							System.out.println("Adding nevefbox: before " + i + " (plus " + nNewNulls + ")");

							this.instructions.insert(insertAfter, new VarInsnNode(TaintUtils.NEVER_AUTOBOX, j));
							nNewNulls++;
						}
					} else if (alwaysAutoBoxByFrame.containsKey(i)) {
						for (int j : alwaysAutoBoxByFrame.get(i)) {
							//							System.out.println("Adding checkcast always: before " + i + " (plus " + nNewNulls + ")");
							//								while(insertAfter.getType() == AbstractInsnNode.LABEL ||
							//										insertAfter.getType() == AbstractInsnNode.LINE||
							//										insertAfter.getType() == AbstractInsnNode.FRAME)
							//									insertAfter = insertAfter.getNext();
							AbstractInsnNode query = insertAfter.getNext();
							while(query.getNext() != null && (query.getType() == AbstractInsnNode.LABEL || query.getType() == AbstractInsnNode.LINE || query.getType() == AbstractInsnNode.FRAME))
								query = query.getNext();
							if(query.getOpcode() == Opcodes.ALOAD && query.getNext().getOpcode() == Opcodes.MONITOREXIT)
								insertAfter = query.getNext();
							if(insertAfter.getType() == AbstractInsnNode.JUMP_INSN)
							{
								insertAfter = insertAfter.getPrevious();
								//								System.out.println("insertbefore  : " + ((JumpInsnNode) insertAfter.getNext()).toString());
								if(insertAfter.getNext().getOpcode() != Opcodes.GOTO)
								{

									this.instructions.insert(insertAfter, new VarInsnNode(TaintUtils.ALWAYS_BOX_JUMP, j));
								}
								else
								{
									//									System.out.println("box immediately");
									this.instructions.insert(insertAfter, new VarInsnNode(TaintUtils.ALWAYS_AUTOBOX, j));
								}
							}
							else
							{
								//								System.out.println("InsertAfter: " + insertAfter);
								this.instructions.insert(insertAfter, new VarInsnNode(TaintUtils.ALWAYS_AUTOBOX, j));
							}
							nNewNulls++;
						}
					}
				}
				//				System.out.println(name+desc);
				//fix LVs for android (sigh)
				//				for(LabelNode l : problemLabels.keySet())
				//				{
				//					System.out.println("Problem label: "+l);
				//				}
				boolean hadChanges = true;
				while (hadChanges) {
					hadChanges = false;

					HashSet<LocalVariableNode> newLVNodes = new HashSet<LocalVariableNode>();
					if (this.localVariables != null) {
						for (LocalVariableNode lv : this.localVariables) {
							AbstractInsnNode toCheck = lv.start;
							LabelNode veryEnd = lv.end;
							while (toCheck != null && toCheck != lv.end) {
								if ((toCheck.getOpcode() == TaintUtils.ALWAYS_BOX_JUMP || toCheck.getOpcode() ==TaintUtils.ALWAYS_AUTOBOX) && ((VarInsnNode) toCheck).var == lv.index) {
									//									System.out.println("LV " + lv.name + " will be a prob around " + toCheck);
									LabelNode beforeProblem = new LabelNode(new Label());
									LabelNode afterProblem = new LabelNode(new Label());
									this.instructions.insertBefore(toCheck, beforeProblem);
									this.instructions.insert(toCheck.getNext(), afterProblem);
									LocalVariableNode newLV = new LocalVariableNode(lv.name, lv.desc, lv.signature, afterProblem, veryEnd, lv.index);
									lv.end = beforeProblem;
									newLVNodes.add(newLV);
									hadChanges = true;
									break;
								}
								toCheck = toCheck.getNext();
							}
						}
						this.localVariables.addAll(newLVNodes);
					}
				}
			} catch (AnalyzerException e) {
				e.printStackTrace();
			}
			this.accept(cmv);
		}

		private void computeVarsAccessed(Integer i, HashMap<Integer, BasicBlock> cfg) {
			if(!cfg.containsKey(i) || cfg.get(i).covered)
				return;
			BasicBlock t = cfg.get(i);
			t.covered = true;
			for(Integer j : t.outEdges)
			{
				if(cfg.containsKey(j)){
					computeVarsAccessed(j, cfg);
					t.varsAccessed.addAll(cfg.get(j).varsAccessed);
				}
			}
		}
	}
	static class BasicBlock{
		int idx;
		LinkedList<Integer> outEdges = new LinkedList<Integer>();
		boolean covered;
		HashSet<Integer> varsAccessed = new HashSet<Integer>();
	}
	private static boolean isPrimitiveArrayType(BasicValue v) {
		if (v == null || v.getType() == null)
			return false;
		return v.getType().getSort() == Type.ARRAY && v.getType().getElementType().getSort() != Type.OBJECT;
	}

	static final boolean DEBUG = false;
	public HashSet<Type> wrapperTypesToPreAlloc = new HashSet<Type>();

	@Override
	public void visitVarInsn(int opcode, int var) {
		/* For def use */
		switch(opcode)
		{
		case Opcodes.ISTORE:
		case Opcodes.ILOAD:
			wrapperTypesToPreAlloc.add(Type.getType(TaintedInt.class));
			break;
		case Opcodes.LSTORE:
		case Opcodes.LLOAD:
			wrapperTypesToPreAlloc.add(Type.getType(TaintedLong.class));
			break;
		case Opcodes.FSTORE:
		case Opcodes.FLOAD:
			wrapperTypesToPreAlloc.add(Type.getType(TaintedFloat.class));
			break;
		case Opcodes.DSTORE:
		case Opcodes.DLOAD:
			wrapperTypesToPreAlloc.add(Type.getType(TaintedDouble.class));
			break;
		}

		super.visitVarInsn(opcode, var);
	}
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		/* For def use */
		switch(opcode)
		{
		case Opcodes.PUTSTATIC:
			if(desc.equals("I"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedInt.class));
			else if(desc.equals("D"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedDouble.class));
			else if(desc.equals("F"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedFloat.class));
			else if(desc.equals( "J"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedLong.class));
			break;
		case Opcodes.GETSTATIC:
		case Opcodes.PUTFIELD:
			if(desc.equals("I"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedInt.class));
			else if(desc.equals("D"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedDouble.class));
			else if(desc.equals("F"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedFloat.class));
			else if(desc.equals( "J"))
				wrapperTypesToPreAlloc.add(Type.getType(TaintedLong.class));
			break;
		case Opcodes.GETFIELD:
		}

		super.visitFieldInsn(opcode, owner, name, desc);
	}
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itfc) {
		super.visitMethodInsn(opcode, owner, name, desc,itfc);
		Type returnType = Type.getReturnType(desc);
		Type newReturnType = TaintUtils.getContainerReturnType(returnType);
		if(newReturnType != returnType && !(returnType.getSort() == Type.ARRAY && returnType.getDimensions() > 1))
			wrapperTypesToPreAlloc.add(newReturnType);
	}

	public PrimitiveArrayAnalyzer(final String className, int access, final String name, final String desc, String signature, String[] exceptions, final MethodVisitor cmv) {
		super(Opcodes.ASM5);
		this.mv = new PrimitiveArrayAnalyzerMN(access, name, desc, signature, exceptions, className, cmv);
	}
	public PrimitiveArrayAnalyzer(Type singleWrapperTypeToAdd) {
		super(Opcodes.ASM5);
		this.mv = new PrimitiveArrayAnalyzerMN(0, null,null,null,null,null, null);
		if(singleWrapperTypeToAdd.getSort() == Type.OBJECT && singleWrapperTypeToAdd.getInternalName().startsWith("edu/columbia/cs/psl/phosphor/struct") && !singleWrapperTypeToAdd.getInternalName().contains("MultiDTainted"))
			this.wrapperTypesToPreAlloc.add(singleWrapperTypeToAdd);
	}

	NeverNullArgAnalyzerAdapter analyzer;

	public void setAnalyzer(NeverNullArgAnalyzerAdapter preAnalyzer) {
		analyzer = preAnalyzer;
	}
}
