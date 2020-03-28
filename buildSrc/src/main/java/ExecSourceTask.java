import groovy.lang.Closure;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Factory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Combines the features of an {@link Exec} task with a {@link SourceTask} to ensure easier specifications of
 * dependencies.
 */
public class ExecSourceTask extends AbstractExecTask<ExecSourceTask> implements PatternFilterable {
	private final List<Object> source = new ArrayList<Object>();
	private final PatternFilterable patternSet;

	public ExecSourceTask() {
		super(ExecSourceTask.class);
		patternSet = getPatternSetFactory().create();
	}

	@Inject
	protected Factory<PatternSet> getPatternSetFactory() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the source for this task, after the include and exclude patterns have been applied. Ignores source files which do not exist.
	 *
	 * <p>
	 * The {@link PathSensitivity} for the sources is configured to be {@link PathSensitivity#ABSOLUTE}.
	 * If your sources are less strict, please change it accordingly by overriding this method in your subclass.
	 * </p>
	 *
	 * @return The source.
	 */
	@InputFiles
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.ABSOLUTE)
	public FileTree getSource() {
		ArrayList<Object> copy = new ArrayList<Object>(this.source);
		FileTree src = getProject().files(copy).getAsFileTree();
		return src.matching(patternSet);
	}

	/**
	 * Sets the source for this task.
	 *
	 * @param source The source.
	 * @since 4.0
	 */
	public void setSource(FileTree source) {
		setSource((Object) source);
	}

	/**
	 * Sets the source for this task. The given source object is evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param source The source.
	 */
	public void setSource(Object source) {
		this.source.clear();
		this.source.add(source);
	}

	/**
	 * Adds some source to this task. The given source objects will be evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param sources The source to add
	 * @return this
	 */
	public ExecSourceTask source(Object... sources) {
		Collections.addAll(this.source, sources);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask include(String... includes) {
		patternSet.include(includes);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask include(Iterable<String> includes) {
		patternSet.include(includes);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask include(Spec<FileTreeElement> includeSpec) {
		patternSet.include(includeSpec);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask include(Closure includeSpec) {
		patternSet.include(includeSpec);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask exclude(String... excludes) {
		patternSet.exclude(excludes);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask exclude(Iterable<String> excludes) {
		patternSet.exclude(excludes);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask exclude(Spec<FileTreeElement> excludeSpec) {
		patternSet.exclude(excludeSpec);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask exclude(Closure excludeSpec) {
		patternSet.exclude(excludeSpec);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Internal
	public Set<String> getIncludes() {
		return patternSet.getIncludes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask setIncludes(Iterable<String> includes) {
		patternSet.setIncludes(includes);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Internal
	public Set<String> getExcludes() {
		return patternSet.getExcludes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecSourceTask setExcludes(Iterable<String> excludes) {
		patternSet.setExcludes(excludes);
		return this;
	}
}
