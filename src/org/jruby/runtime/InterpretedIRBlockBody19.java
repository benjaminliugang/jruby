package org.jruby.runtime;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyModule;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.compiler.ir.IRClosure;
import org.jruby.interpreter.Interpreter;
import org.jruby.runtime.Block.Type;
import org.jruby.runtime.builtin.IRubyObject;

public class InterpretedIRBlockBody19 extends InterpretedIRBlockBody {
    public InterpretedIRBlockBody19(IRClosure closure, Arity arity, int argumentType) {
        super(closure, arity, -1);
    }

    private IRubyObject[] convertValueIntoArgArray(ThreadContext context, IRubyObject value, boolean isArray) {
        // Eliminate the additional array-wrap when arrays are being passed in
        if (isArray) {
            RubyArray valArray = (RubyArray)value;
            if (valArray.size() == 1) {
                value = valArray.eltInternal(0);
                isArray = false;
            }
        }

        int blockArity = arity().getValue();
        switch (blockArity) {
            case 0  : return IRubyObject.NULL_ARRAY;
            case 1  : return isArray ? new IRubyObject[] { ((RubyArray)value).eltInternal(0) } : new IRubyObject[] { value };
            case -1 : return isArray ? ((RubyArray)value).toJavaArray() : new IRubyObject[] { value };
            default : 
               if (!isArray) {
                   value = RuntimeHelpers.aryToAry(value);
                   if (!(value instanceof RubyArray)) {
                       throw context.getRuntime().newTypeError(value.getType().getName() + "#to_ary should return Array");
                   }
               }
               return ((RubyArray)value).toJavaArray();
        }
    }

    @Override
    public IRubyObject yield(ThreadContext context, IRubyObject value, IRubyObject self, RubyModule klass, boolean isArray, Binding binding, Type type) {
        IRubyObject[] args = (value == null) ? IRubyObject.NULL_ARRAY : convertValueIntoArgArray(context, value, isArray);
        return commonYieldPath(context, args, self, klass, binding, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject[] prepareArgumentsForCall(ThreadContext context, IRubyObject[] args, Block.Type type) {
        int blockArity = arity().getValue();

        switch (type) {
        // SSS FIXME: How is it even possible to "call" a block?  
        // I thought only procs & lambdas can be called, and blocks are yielded to.
        case NORMAL: 
        case PROC: {
            if (args.length == 1) {
                args = convertValueIntoArgArray(context, args[0], false);
            } else if (blockArity == 1) {
                args = convertToRubyArray(context, args);
            }
            break;
        }
        case LAMBDA:
            if (blockArity == 1 && args.length != 1) {
                if (blockArity != args.length) {
                    context.getRuntime().getWarnings().warn(ID.MULTIPLE_VALUES_FOR_BLOCK, "multiple values for a block parameter (" + args.length + " for " + blockArity + ")");
                }
                args = convertToRubyArray(context, args);
            } else {
                arity().checkArity(context.getRuntime(), args);
            }
            break;
        }

        return args;
    }
}
