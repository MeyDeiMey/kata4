package software.ulpgc.model;

import java.io.IOException;
import java.util.List;

public interface TitleReader {
    List<Title> read() throws IOException;
}