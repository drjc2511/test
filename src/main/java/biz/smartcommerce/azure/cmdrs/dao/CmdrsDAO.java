package biz.smartcommerce.azure.cmdrs.dao;

import java.util.List;

import biz.smartcommerce.azure.cmdrs.model.CmdrsEntry;

public interface CmdrsDAO {
    /**
     * @return A list of CmdrsEntries
     */
    public List<CmdrsEntry> readCmdrsEntries();

    /**
     * @param cmdrsEntry
     * @return whether the cmdrsEntry was persisted.
     */
    public CmdrsEntry createCmdrsEntry(CmdrsEntry cmdrsEntry);
    
    /**
     * @param Json String
     * @return whether the cmdrsEntry was persisted.
     */
    public CmdrsEntry createCmdrsEntry(String json);

    /**
     * @param id
     * @return the cmdrsEntry
     */
    public CmdrsEntry readCmdrsEntry(String id);

    /**
     * @param id
     * @return the cmdrsEntry
     */
    public CmdrsEntry updateCmdrsEntry(String id, boolean isComplete);

    /**
     *
     * @param id
     * @return whether the delete was successful.
     */
    public boolean deleteCmdrsEntry(String id);
}
