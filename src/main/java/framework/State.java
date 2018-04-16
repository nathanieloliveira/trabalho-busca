package framework;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.List;

public interface State<T extends State> {
    @NotNull
    List<T> generateSuccessors();
    int calculateHeuristic(@Nullable T goal);
}
