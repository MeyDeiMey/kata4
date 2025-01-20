package software.ulpgc.model;

import java.util.Optional;

public interface TitleDeserializer {
    Optional<Title> deserialize(String line);

}