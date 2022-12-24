package tla.web.model.mappings;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import tla.domain.dto.meta.AbstractDto;
import tla.domain.dto.meta.DocumentDto;
import tla.domain.dto.meta.NamedDocumentDto;
import tla.domain.model.SentenceToken;
import tla.domain.model.meta.BTSeClass;
import tla.domain.model.meta.TLADTO;
import tla.web.config.ApplicationProperties;
import tla.web.model.meta.BTSObject;
import tla.web.model.meta.ModelClass;
import tla.web.model.meta.TLAObject;
import tla.web.model.parts.Glyphs;
import tla.web.model.parts.Token;

/**
 * This component is used to keep record of domain model classes and their DTO type
 * counterparts associated with them via their respective {@link BTSeClass} and {@link TLADTO} annotations.
 * This enables object services to convert incoming DTO sent by the TLA backend into instances of corresponding
 * classes from the web frontend model (using )
 *
 * <p>Domain model classes get registered by their respective object service, as long as the service's default
 * constructor is being invoked (e.g. by virtue of being a spring service). Such a domain model class
 * <strong>must</strong> be annotated with a {@link BTSeClass} annotation in order to enable object services
 * to process DTO responses from the TLA backend (services use the method {@link #convertDTO(AbstractDto)}
 * for this). Model classes should also be annotated with a {@link TLADTO} annotation specifying the DTO
 * type they correspond to, so that a model mapping can automatically be created for conversion from such DTO
 * to domain model class instances. </p>
 *
 * <p>Generic mappings can be specified for DTO and domain model superclasses in {@link #initModelMapper()},
 * and then automatically be inherited by subclassing domain model classes (cf.
 * {@link #registerModelMapping(Class, Class)})). Specific model mappings can also be customized in
 * {@link #initModelMapper()}. However, mappings for model components must be declared before mappings for
 * model classes containing those components (e.g. token mappings must be declared <em>before</em> lemma and
 * sentence mappings, because token mappings would reset lemma and sentence mappings otherweise). </p>
 */
@Slf4j
@Configuration
public class MappingConfig {

    private static ModelMapper modelMapper;

    private static Map<String, Class<? extends TLAObject>> modelClasses = new HashMap<>();

    private ExternalReferencesConverter externalReferencesConverter;

    @Autowired
    public MappingConfig(ExternalReferencesConverter externalReferencesConverter) {
        this.externalReferencesConverter = externalReferencesConverter;
    }

    @Bean
    public ModelMapper modelMapper() {
        return modelMapper != null ? modelMapper : this.initModelMapper();
    }

    /**
     * Creates a new model mapper and configures type maps for converting DTO to domain model
     * class instances.
     */
    protected ModelMapper initModelMapper() {
        modelMapper = new ModelMapper();
        log.info("registered model classes: {}", modelClasses.values());
        /* general base type mappings */
        modelMapper.createTypeMap(NamedDocumentDto.class, BTSObject.class).addMappings(
            m -> m.using(externalReferencesConverter).map(
                NamedDocumentDto::getExternalReferences,
                BTSObject::setExternalReferences
            )
        );
        modelMapper.createTypeMap(DocumentDto.class, BTSObject.class).addMapping(
            DocumentDto::getEditors, BTSObject::setEdited
        );
        /* specific type mappings */
        modelMapper.typeMap(SentenceToken.class, Token.class).addMappings(
            m -> m.using(
                new TokenGlyphsToSVGConverter()
            ).<String>map(
                token -> token, (dest, result) -> dest.getGlyphs().setSvg(result)
            )
        );
        /* add mappings for registered model classes and apply base type mappings */
        this.registerModelMappings();
        return modelMapper;
    }

    /**
     * Returns all model classes previously registered via {@link #registerModelClass(Class)}.
     */
    public static Collection<Class<? extends TLAObject>> getRegisteredModelClasses() {
        return modelClasses.values();
    }

    /**
     * Register mappings for all previously registered model classes..
     */
    private void registerModelMappings() {
        for (Class<? extends TLAObject> modelClass : getRegisteredModelClasses()) {
            for (Annotation a : modelClass.getAnnotationsByType(TLADTO.class)) {
                registerModelMapping(((TLADTO) a).value(), modelClass);
            }
        }
    }

    /**
     * Produces the model mapper's type map for the specified DTO type and model class.
     * If the model mapper doesn't already have a type map for those two specific classes,
     * it will create an empty one and return the result.
     */
    private TypeMap<?, ?> getDTOModelTypeMap(
        Class<? extends AbstractDto> dtoClass, Class<? extends TLAObject> modelClass
    ) {
        TypeMap<?, ?> typemap = modelMapper().getTypeMap(
            dtoClass.asSubclass(AbstractDto.class), modelClass.asSubclass(TLAObject.class)
        );
        if (typemap == null) {
            typemap = modelMapper().createTypeMap(
                dtoClass.asSubclass(AbstractDto.class), modelClass.asSubclass(TLAObject.class)
            );
        }
        return typemap;
    }

    /**
     * Register mappings from DTO to domain model class instances by creating a type map
     * in the {@link ModelMapper} for the specified classes.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <D extends AbstractDto, T extends TLAObject> TypeMap<D, T> registerModelMapping(
        Class<D> dtoClass, Class<T> modelClass
    ) {
        log.info("register model mappings from {} to {}", dtoClass, modelClass);
        TypeMap typemap = getDTOModelTypeMap(dtoClass, modelClass);
        if (BTSObject.class.isAssignableFrom(modelClass)) {
            if (DocumentDto.class.isAssignableFrom(dtoClass)) {
                typemap.includeBase(DocumentDto.class, BTSObject.class);
            }
            if (NamedDocumentDto.class.isAssignableFrom(dtoClass)) {
                typemap.includeBase(NamedDocumentDto.class, BTSObject.class);
            }
        }
        return typemap;
    }

    /**
     * get eclass specifier either from {@link BTSeClass} annotation or via {@link TLADTO} annotation.
     */
    public static String extractEclass(Class<?> clazz) {
        return tla.domain.model.meta.Util.extractEclass(clazz);
    }

    /**
     * Registers a given model class under the <code>eclass</code> value specified in
     * the {@link BTSeClass} annotation of the model class, <em>or</em> of the DTO class
     * specified in the {@link TLADTO} annotation of the model class.
     *
     * @param modelClass some {@link TLAObject} subclass with {@link BTSeClass} or {@link TLADTO} annotation
     */
    public static void registerModelClass(Class<? extends TLAObject> modelClass) {
        modelClasses.put(extractEclass(modelClass), modelClass);
    }

    /**
     * Look up application domain model class corresponding to the specified
     * <code>eclass</code>. Domain model classes must be registered using
     * a {@link ModelClass} annotation in order to be
     * able to be looked up. Also they need to have a {@link BTSeClass} annotation
     * obviously.
     *
     * @param eclass BTS <code>eclass</code> identifier
     * @return {@link TLAObject} subclass from the domain model
     */
    public static Class<? extends TLAObject> getModelClass(String eclass) {
        return modelClasses.get(eclass);
    }

    /**
     * Maps a DTO to the corresponding application domain model class,
     * which gets determined by the DTO's <code>eclass</code>.
     * In order to be able to look up the model class, it must be among
     * the values passed via {@link ModelClasses} annotation on top of
     * {@link MappingConfig}.
     *
     * @param dto an {@link AbstractDto} instance
     * @return model class ({@link TLAObject} subclass) corresponding to the DTO's <code>eclass</code>
     */
    public static TLAObject convertDTO(AbstractDto dto) {
        try {
            return modelMapper.map(
                dto,
                getModelClass(dto.getEclass())
            );
        } catch (IllegalArgumentException e) {
            log.error("No model mapping for DTO type {}!", dto.getEclass());
            return new TLAObject(){};
        } catch (NullPointerException e) {
            log.error("Could not map DTO to web model. Model mapper not yet initialized!");
            return null;
        }
    }

}
