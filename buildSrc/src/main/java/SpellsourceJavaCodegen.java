import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.JavaClientCodegen;
import io.swagger.models.properties.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

class SpellsourceJavaCodegen extends JavaClientCodegen {
	public SpellsourceJavaCodegen() {
		super();
		apiTestTemplateFiles.clear();
		projectFolder = "src" + File.separator + "swagger";
		sourceFolder = projectFolder + File.separator + "java";
		var list = new ArrayList<>(supportedLibraries.keySet());
		list.remove("jersey1");
		for (var item : list) {
			supportedLibraries.remove(item);
		}
		setLibrary("jersey1");
	}

	@Override
	public void processOpts() {
		super.processOpts();

		supportingFiles.removeIf(supportingFile -> !supportingFile.destinationFilename.endsWith("java"));
	}
}
