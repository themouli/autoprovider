package in.workarounds.autoprovider.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import in.workarounds.autoprovider.compiler.utils.ClassUtils;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

/**
 * Created by mouli on 10/22/15.
 */
public class CursorGenerator {

    private final String ERROR_MESSAGE_NULL = "The value of '%s' in the database was null, which is not allowed according to the model definition";
    private final ClassName ABSTRACT_CURSOR = ClassName.get("in.workarounds.autoprovider", "AbstractCursor");

    private final MethodSpec CONSTRUCTOR;
    List<MethodSpec> methods = new ArrayList<>();

    public CursorGenerator(AnnotatedTable annotatedTable) {

        CONSTRUCTOR = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CURSOR, "cursor")
                .addStatement("super(cursor)")
                .build();

        for(AnnotatedColumn annotatedColumn: annotatedTable.getColumns()) {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(String.format("get%s",
                    StringUtils.toCamelCase(annotatedColumn.getColumnName())))
                    .addAnnotation(ClassUtils.NONNULL)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(annotatedColumn.getTypeInObject()))
                    .addCode(getColumnInitializer(annotatedTable.getTableName(), annotatedColumn));

            if(annotatedColumn.isNotNull()) {
                String nullMessage = String.format(ERROR_MESSAGE_NULL, annotatedColumn.getColumnName());
                builder.beginControlFlow("if( res == null) ")
                        .addStatement("throw new NullPointerException($S)", nullMessage)
                        .endControlFlow();
            }
            builder.addStatement("return res");
            methods.add(builder.build());
        }
    }

    private CodeBlock getColumnInitializer(String tableName, AnnotatedColumn annotatedColumn) {
        String typeInObject = annotatedColumn.getTypeInObject().toString();
        String splits[] = typeInObject.split("\\.");
        String simpleTypeName = splits[splits.length-1];
        CodeBlock codeBlock = CodeBlock.builder()
                .add("$T res = get$LOrNull($L.$L);\n", annotatedColumn.getTypeInObject(),
                        StringUtils.toCamelCase(simpleTypeName), StringUtils.toCamelCase(tableName), annotatedColumn.getColumnName().toUpperCase())
                .build();
        return codeBlock;
    }

    public JavaFile generateTable(String outputPackage, String outputName) {
        TypeSpec outputTable = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputTable).build();
    }

    public TypeSpec buildClass(String name) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ABSTRACT_CURSOR);

        builder.addMethod(CONSTRUCTOR);

        methods.forEach(builder::addMethod);

        return builder.build();
    }
}
