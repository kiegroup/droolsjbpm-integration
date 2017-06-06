package org.kie.maven.plugin;

import com.google.protobuf.ByteString;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.PackageRegistry;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieModuleCache;
import org.drools.compiler.kie.builder.impl.KieModuleCacheHelper;
import org.drools.core.factmodel.ClassDefinition;
import org.drools.core.rule.JavaDialectRuntimeData;
import org.drools.core.rule.KieModuleMetaInfo;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.rule.TypeMetaInfo;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class KieInMemoryMetaInfoBuilder {

    private final MemoryFileSystem trgMfs;
    private final InternalKieModule kModule;

    public KieInMemoryMetaInfoBuilder(MemoryFileSystem trgMfs, InternalKieModule kModule) {
        this.trgMfs = trgMfs;
        this.kModule = kModule;
    }

    public KieModuleMetaInfo getKieModuleMetaInfo() {
        KieModuleMetaInfo info = this.generateKieModuleMetaInfo();
        //     this.trgMfs.write("META-INF/kmodule.info", info.marshallMetaInfos().getBytes(IoUtils.UTF8_CHARSET), true);
        return info;
    }

    public MemoryFileSystem getMemoryFileSystem() {
        return this.trgMfs;
    }

    private KieModuleMetaInfo generateKieModuleMetaInfo() {
        Map<String, TypeMetaInfo> typeInfos = new HashMap();
        Map<String, Set<String>> rulesPerPackage = new HashMap();
        KieModuleModel kieModuleModel = this.kModule.getKieModuleModel();
        Iterator var4 = kieModuleModel.getKieBaseModels().keySet().iterator();

        while (var4.hasNext()) {
            String kieBaseName = (String) var4.next();
            KnowledgeBuilderImpl kBuilder = (KnowledgeBuilderImpl) this.kModule.getKnowledgeBuilderForKieBase(kieBaseName);
            Map<String, PackageRegistry> pkgRegistryMap = kBuilder.getPackageRegistry();
            KieModuleCache.KModuleCache.Builder _kmoduleCacheBuilder = this.createCacheBuilder();
            KieModuleCache.CompilationData.Builder _compData = this.createCompilationData();

            JavaDialectRuntimeData runtimeData;
            ArrayList types;
            for (Iterator var10 = kBuilder.getKnowledgePackages().iterator(); var10.hasNext(); this.addToCompilationData(_compData, runtimeData, types)) {
                KiePackage kPkg = (KiePackage) var10.next();
                PackageRegistry pkgRegistry = (PackageRegistry) pkgRegistryMap.get(kPkg.getName());
                runtimeData = (JavaDialectRuntimeData) pkgRegistry.getDialectRuntimeRegistry().getDialectData("java");
                types = new ArrayList();

                String internalName;
                for (Iterator var15 = kPkg.getFactTypes().iterator(); var15.hasNext(); types.add(internalName)) {
                    FactType factType = (FactType) var15.next();
                    Class<?> typeClass = ((ClassDefinition) factType).getDefinedClass();
                    TypeDeclaration typeDeclaration = pkgRegistry.getPackage().getTypeDeclaration(typeClass);
                    if (typeDeclaration != null) {
                        typeInfos.put(typeClass.getName(), new TypeMetaInfo(typeDeclaration));
                    }

                    String className = factType.getName();
                    internalName = className.replace('.', '/') + ".class";
                    byte[] bytes = runtimeData.getBytecode(internalName);
                    if (bytes != null) {
                        this.trgMfs.write(internalName, bytes, true);
                    }
                }

                Set<String> rules = (Set) rulesPerPackage.get(kPkg.getName());
                if (rules == null) {
                    rules = new HashSet();
                }

                Iterator var23 = kPkg.getRules().iterator();

                while (var23.hasNext()) {
                    Rule rule = (Rule) var23.next();
                    if (!((Set) rules).contains(rule.getName())) {
                        ((Set) rules).add(rule.getName());
                    }
                }

                if (!((Set) rules).isEmpty()) {
                    rulesPerPackage.put(kPkg.getName(), rules);
                }
            }

            _kmoduleCacheBuilder.addCompilationData(_compData.build());
            this.writeCompilationDataToTrg(_kmoduleCacheBuilder.build(), kieBaseName);
        }

        return new KieModuleMetaInfo(typeInfos, rulesPerPackage);
    }

    private KieModuleCache.KModuleCache.Builder createCacheBuilder() {
        return KieModuleCache.KModuleCache.newBuilder();
    }

    private KieModuleCache.CompilationData.Builder createCompilationData() {
        return KieModuleCache.CompilationData.newBuilder().setDialect("java");
    }

    private void addToCompilationData(KieModuleCache.CompilationData.Builder _cdata, JavaDialectRuntimeData runtimeData, List<String> types) {
        Iterator var4 = runtimeData.getStore().entrySet().iterator();

        while (var4.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry) var4.next();
            if (!types.contains(entry.getKey())) {
                KieModuleCache.CompDataEntry _entry = KieModuleCache.CompDataEntry.newBuilder().setId((String) entry.getKey()).setData(ByteString.copyFrom((byte[]) entry.getValue())).build();
                _cdata.addEntry(_entry);
            }
        }

    }

    private void writeCompilationDataToTrg(KieModuleCache.KModuleCache _kmoduleCache, String kieBaseName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            KieModuleCacheHelper.writeToStreamWithHeader(out, _kmoduleCache);
            String compilationDataPath = "META-INF/" + kieBaseName.replace('.', '/') + "/kbase.cache";
            this.trgMfs.write(compilationDataPath, out.toByteArray(), true);
        } catch (IOException var5) {

        }

    }
}
