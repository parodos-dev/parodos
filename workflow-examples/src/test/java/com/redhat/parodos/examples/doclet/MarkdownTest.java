package com.redhat.parodos.examples.doclet;

import java.util.Locale;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class MarkdownTest {

	@Mock
	Reporter reporter;

	@Mock
	DocletEnvironment docletEnvironment;

	@BeforeEach
	public void setUp() {
		openMocks(this);
	}

	@Test
	public void canRunDoclet() {
		Markdown markdown = new Markdown();
		markdown.init(Locale.getDefault(), reporter);

		markdown.run(docletEnvironment);
		verify(docletEnvironment, atLeastOnce()).getDocTrees();
	}

}
