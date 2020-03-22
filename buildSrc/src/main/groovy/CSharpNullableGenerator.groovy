import io.swagger.codegen.CodegenProperty
import io.swagger.codegen.languages.CSharpClientCodegen

class CSharpNullableGenerator extends CSharpClientCodegen {
    @Override
    String getTypeDeclaration(io.swagger.models.properties.Property p) {
        def declaration = super.getTypeDeclaration(p)
        if (!Boolean.parseBoolean(p.vendorExtensions.getOrDefault("x-nullable", "true").toString()) && declaration.contains("?")) {
            return declaration.replaceAll("\\?", "")
        }
        return declaration
    }

    @Override
    void updateCodegenPropertyEnum(CodegenProperty var) {
        super.updateCodegenPropertyEnum(var)
        if (var.vendorExtensions.containsKey("x-enum-varnames")) {
            List<Map<String, String>> enumVars = (List<Map<String, String>>) var.allowableValues.get("enumVars");
            def varNames = (ArrayList) var.vendorExtensions.get("x-enum-varnames");
            for (int i = 0; i < enumVars.size(); i++) {
                enumVars.get(i).put("xEnumVarname", (String) varNames.get(i));
            }
        }
    }
}