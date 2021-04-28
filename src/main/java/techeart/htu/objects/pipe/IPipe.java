package techeart.htu.objects.pipe;

import java.util.Set;

public interface IPipe
{
    IPipeGrid getGrid();
    void setGrid(IPipeGrid grid);

    void createGrid();
    void updateGrid();

    Set<PipeConnectionFluid> getConnections();
}
