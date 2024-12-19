/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2024 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.javadoc;

import static com.github.javaparser.StaticJavaParser.parse;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class JavadocExtractorTest {

    @Test
    void canParseAllJavadocsInJavaParser() throws FileNotFoundException {
        processDir(new File(".."));
    }

    private void processFile(File file) throws FileNotFoundException {
        try {
            StaticJavaParser.getParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21).setLexicalPreservationEnabled(true);
            CompilationUnit cu = parse(file);
            new VoidVisitorAdapter<>() {
                @Override
                public void visit(JavadocComment n, Object arg) {
                    super.visit(n, arg);
                    n.parse();
                }
            }.visit(cu, null);
        } catch (ParseProblemException e) {
            switch (file.getName()){
                //These files contain invalid java syntax, so won't be parsed.
                case "Sample.java", "TestFileIso88591.java", "EnumWithInnerType.java"-> {
                    String msg = "INVALID SYNTAX in " + file + ". Problem: " + e.getProblems().getFirst().getMessage();
                    System.out.println(msg);
                }
                default -> fail("ERROR PROCESSING " + file + ". Cause: " + e.getMessage());
            }

        }
    }

    private void processDir(File dir) throws FileNotFoundException {
        for (File child : Objects.requireNonNull(dir.listFiles())) {
            if (child.isFile() && child.getName().endsWith(".java")) {
                processFile(child);
            } else if (child.isDirectory()) {
                processDir(child);
            }
        }
    }
}
