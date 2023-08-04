package com.github.gradle.node.variant;

import com.github.gradle.node.NodeExtension;
import com.github.gradle.node.variant.VariantComputer;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public class NodeNpm {
	private static VariantComputer variantComputer = new VariantComputer();

	public static Provider<String> npmBinary(Project project) {
		var nodeExtension = NodeExtension.get(project);
		var nodeDir = variantComputer.computeNodeDir(NodeExtension.get(project));
		var npmDir = variantComputer.computeNpmDir(nodeExtension, nodeDir);
		var npmBinDir = variantComputer.computeNpmBinDir(npmDir);
		return variantComputer.computeNpmExec(nodeExtension, npmBinDir);
	}
}
