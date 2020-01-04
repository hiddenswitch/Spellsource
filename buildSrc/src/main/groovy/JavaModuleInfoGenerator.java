import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.JavaClientCodegen;

class JavaModuleInfoGenerator extends JavaClientCodegen {
	@Override
	public void processOpts() {
		super.processOpts();
		supportingFiles.add(new SupportingFile("module-info.java.mustache", sourceFolder, "module-info.java"));
	}
}
