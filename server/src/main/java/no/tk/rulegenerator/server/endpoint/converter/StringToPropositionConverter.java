package no.tk.rulegenerator.server.endpoint.converter;

import com.google.gson.Gson;
import java.util.Collections;
import java.util.Set;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNProposition;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

@Component
public class StringToPropositionConverter implements ConditionalGenericConverter {

  // Needed for custom form data deserialization
  private final Gson gson = new Gson();

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return targetType.getAnnotation(RequiresConversion.class) != null;
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, BPMNProposition.class));
  }

  @Override
  public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    return gson.fromJson((String) source, BPMNProposition.class);
  }
}
