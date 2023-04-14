package tech.dzolotov.sampleksp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import tech.dzolotov.sampleksp.annotation.SampleAnnotation

class SampleAnnotationProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    val processed = mutableListOf<ClassName>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val declarations = resolver.getSymbolsWithAnnotation(SampleAnnotation::class.qualifiedName!!, inDepth = false)
            .filterIsInstance<KSClassDeclaration>()

        declarations.forEach { declaration ->
            val classSpec = declaration.asType(listOf()).toClassName()
            //избегаем двойной обработки аннотаций
            if (!processed.contains(classSpec)) {
                processed.add(classSpec)
                //получаем название пакета и класса
                val packageName = classSpec.packageName
                val className = classSpec.simpleName
                //извлекаем типы и названия полей исходного класса
                val properties = declaration.getAllProperties()
                //и создаем список свойств и основной конструктор
                val poetProperties = mutableListOf<PropertySpec>()
                val constructorParams = mutableListOf<ParameterSpec>()
                val resultTemplate = mutableListOf<String>()
                properties.forEach {
                    val name = it.simpleName.getShortName()
                    poetProperties.add(
                        PropertySpec.builder(name, it.type.resolve().toClassName()).initializer(name).build()
                    )
                    constructorParams.add(ParameterSpec(name, it.type.resolve().toClassName()))
                    resultTemplate.add("$name=\$$name")
                }
                val annotatedClassName = "Annotated$className"
                //теперь генерируем функцию toString и наполняем ее кодом
                val toStringCode =
                    CodeBlock.builder().addStatement("""return "${resultTemplate.joinToString(", ")}"""").indent()
                        .build()
                val toStringFunc =
                    FunSpec.builder("toString").returns(STRING).addModifiers(KModifier.OVERRIDE).addCode(toStringCode)
                        .build()
                val generatedClass =
                    TypeSpec.classBuilder(annotatedClassName).addProperties(poetProperties).primaryConstructor(
                        FunSpec.constructorBuilder().addParameters(constructorParams).build()
                    ).addFunction(toStringFunc).build()

                //генерируем исходный код
                val file = FileSpec.builder(packageName, "$annotatedClassName.kt").addType(
                    generatedClass
                ).build()
                //и записываем его в build/generated/ksp/main/kotlin/<package>/Annotated$className.kt
                val codeFile = environment.codeGenerator.createNewFile(
                    dependencies = Dependencies(false, declaration.containingFile!!),
                    packageName = packageName,
                    fileName = annotatedClassName,
                    extensionName = "kt"
                )
                val writer = codeFile.bufferedWriter()
//                writer.append("//Generated file")
                file.writeTo(writer)
                //не забываем сохранить буфер в файл
                writer.flush()
                writer.close()
            }
        }
        return declarations.toList()
    }

    override fun finish() {
        environment.logger.info("Annotation processor is finished")
    }
}

class SampleAnnotationProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SampleAnnotationProcessor(environment)
}
