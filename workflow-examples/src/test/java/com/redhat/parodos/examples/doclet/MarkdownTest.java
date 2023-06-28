package com.redhat.parodos.examples.doclet;

import java.util.Locale;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MarkdownTest {

	@Mock
	Reporter reporter;

	@Mock
	DocletEnvironment docletEnvironment;

	@Test
	public void canRunDoclet() {
		Markdown markdown = new Markdown();
		markdown.init(Locale.getDefault(), reporter);

		markdown.run(docletEnvironment);
		verify(docletEnvironment, atLeastOnce()).getDocTrees();
	}

}