package com.redhat.parodos.examples.doclet;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.ElementScanner9;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

public class Markdown implements Doclet {

	private DocTrees treeUtils;

	@Override
	public void init(Locale locale, Reporter reporter) {
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public Set<? extends Option> getSupportedOptions() {
		return Set.of();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	private static final boolean OK = true;

	@Override
	public boolean run(DocletEnvironment environment) {
		treeUtils = environment.getDocTrees();
		ShowTags st = new ShowTags(System.out);
		st.show(environment.getSpecifiedElements());
		return OK;
	}

	/**
	 * A scanner to search for elements with documentation comments, and to examine those
	 * comments for custom tags.
	 */
	class ShowTags extends ElementScanner9<Void, Integer> {

		final PrintStream out;

		ShowTags(PrintStream out) {
			this.out = out;
		}

		void show(Set<? extends Element> elements) {
			scan(elements, 0);
		}

		@Override
		public Void scan(Element e, Integer depth) {
			DocCommentTree dcTree = treeUtils.getDocCommentTree(e);
			if (dcTree != null) {
				String indent = "  ".repeat(depth);
				out.println(indent + "| " + e.getKind() + " " + e + " annotations has configuration "
						+ e.getAnnotationMirrors().toArray().toString());

				if (e.getAnnotationMirrors().stream()
						.anyMatch(a -> a.toString().equals("@org.springframework.context.annotation.Configuration"))) {
					try {
						Files.write(Paths.get(e.getSimpleName() + ".md"), dcTree.getFullBody().toString().getBytes());
					}
					catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				}
				Map<String, List<String>> tags = new TreeMap<>();
				new TagScanner(tags).visitDocComment(dcTree, null);
				tags.forEach((t, l) -> {
					out.println(indent + "  @" + t);
					l.forEach(c -> out.println(indent + "    " + c));
				});
			}
			// records are not supported by the tool for some reason. Probably a javadoc
			// bug.
			if (e.getKind().toString().equals("RECORD")) {
				return null;
			}
			return super.scan(e, depth + 1);
		}

	}

	/**
	 * A visitor to gather the block tags found in a comment.
	 */
	class TagScanner extends SimpleDocTreeVisitor<Void, Void> {

		private final Map<String, List<String>> tags;

		TagScanner(Map<String, List<String>> tags) {
			this.tags = tags;
		}

		@Override
		public Void visitDocComment(DocCommentTree tree, Void p) {
			return visit(tree.getBlockTags(), null);
		}

		@Override
		public Void visitUnknownBlockTag(UnknownBlockTagTree tree, Void p) {
			String name = tree.getTagName();
			String content = tree.getContent().toString();
			tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
			return null;
		}

	}

}