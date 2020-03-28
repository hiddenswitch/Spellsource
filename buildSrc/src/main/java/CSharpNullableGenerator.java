import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.languages.CSharpClientCodegen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class CSharpNullableGenerator extends CSharpClientCodegen {
	@Override
	public String getTypeDeclaration(io.swagger.models.properties.Property p) {
		var declaration = super.getTypeDeclaration(p);
		if (!Boolean.parseBoolean(p.getVendorExtensions().getOrDefault("x-nullable", "true").toString()) && declaration.contains("?")) {
			return declaration.replaceAll("\\?", "");
		}
		return declaration;
	}

	@Override
	public void updateCodegenPropertyEnum(CodegenProperty var) {
		super.updateCodegenPropertyEnum(var);
		if (var.vendorExtensions.containsKey("x-enum-varnames")) {
			@SuppressWarnings("unchecked")
			List<Map<String, String>> enumVars = (List<Map<String, String>>) var.allowableValues.get("enumVars");
			var varNames = (ArrayList) var.vendorExtensions.get("x-enum-varnames");
			for (int i = 0; i < enumVars.size(); i++) {
				enumVars.get(i).put("xEnumVarname", (String) varNames.get(i));
			}
		}
	}

	@Override
	public void processOpts() {
		super.processOpts();
		String packageFolder = sourceFolder + File.separator + packageName;
		String clientPackageDir = packageFolder + File.separator + clientPackage;
		supportingFiles.removeIf(supportingFile -> !supportingFile.destinationFilename.endsWith("cs"));
		supportingFiles.removeIf(supportingFile -> supportingFile.folder.equals(clientPackageDir));
		modelDocTemplateFiles.clear();
		apiDocTemplateFiles.clear();
	}
}