/************************************************************
 *
 *  MIT License
 *
 *  Copyright (c) 2017 Mike Robinson
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 *  LuajRepl.java
 *
 *  Main class
 *
 ************************************************************/

package net.fmzlabs.luajrepl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * LuajRepl
 *
 */
public class LuajRepl {

    private enum ReplResult {
        SUCCESS, FAIL, EXIT
    }

    private Globals         globals;
    private BufferedReader  in;
    private PrintStream     out;
    private PrintStream     err;
    private String          chunk;

    private ReplResult evalLua() {
        try {
            globals.load(chunk).invoke();
            return ReplResult.SUCCESS;
        } catch (Exception e) {
            err.println("LuajRepl: Failed to evaluate");
            return ReplResult.FAIL;
        }
    }

    private ReplResult readInput() {
        try {
            chunk = in.readLine();
            if (chunk == null) {
                return ReplResult.EXIT;
            }
            return ReplResult.SUCCESS;
        } catch (IOException e) {
            err.println("LuajRepl: Failed to read");
            chunk = null;
            return ReplResult.FAIL;
        }
    }

    private void showString(String string) {
        out.print(string);
        out.flush();
    }

    private void showBreak() {
        out.print("\n");
        out.flush();
    }

    private void showOutput() {
        out.flush();
    }

    private void mainLoop() {
        ReplResult result = ReplResult.SUCCESS;

        showString("LuajRepl version 0.1\n");

        /* Loop */
        while (true) {
            showString("> ");

            /* Read */
            result = readInput();
            if (result == ReplResult.EXIT) {
                showBreak();
                return;
            } else if (result == ReplResult.FAIL) {
                continue;
            }

            /* Evaluate */
            result = evalLua();
            if (result == ReplResult.FAIL) {
                continue;
            }

            /* Print */
            showOutput();
        }
    }

    private void setupStdIO() {
        in = new BufferedReader(new InputStreamReader(System.in));
        globals.STDIN = null;

        out = new PrintStream(System.out);
        globals.STDOUT = out;

        err = System.err;
        globals.STDERR = err;
    }

    private void setupLuaEnv() {
        globals = JsePlatform.standardGlobals();
    }

    public static void main(String[] args) {
        LuajRepl repl = new LuajRepl();
        repl.setupLuaEnv();
        repl.setupStdIO();
        repl.mainLoop();
    }
}
