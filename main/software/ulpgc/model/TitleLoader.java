package software.ulpgc.model;

import java.sql.SQLException;
import java.util.List;

public interface TitleLoader {
    void save(List<Title> titles) throws SQLException;
    List<Title> load() throws SQLException;
}