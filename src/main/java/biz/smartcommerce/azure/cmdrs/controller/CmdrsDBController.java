package biz.smartcommerce.azure.cmdrs.controller;

import java.util.List;

import biz.smartcommerce.azure.cmdrs.dao.CmdrsDAO;
import biz.smartcommerce.azure.cmdrs.dao.CmdrsDaoFactory;
import biz.smartcommerce.azure.cmdrs.model.CmdrsEntry;
import lombok.NonNull;

public class CmdrsDBController {
    public static CmdrsDBController getInstance() {
        if (cmdrsDBController == null) {
        	cmdrsDBController = new CmdrsDBController(CmdrsDaoFactory.getDao());
        }
        return cmdrsDBController;
    }

    private static CmdrsDBController cmdrsDBController;

    private final CmdrsDAO cmdrsDAO;

    CmdrsDBController(CmdrsDAO cmdrsDAO) {
        this.cmdrsDAO = cmdrsDAO;
    }

    public CmdrsEntry createCmdrsEntry(@NonNull String name,
            @NonNull String category, boolean isComplete) {
    	CmdrsEntry cmdrsEntry = new CmdrsEntry();
    	cmdrsEntry.setName(name);
    	cmdrsEntry.setCategory(category);
    	cmdrsEntry.setComplete(isComplete);
        return cmdrsDAO.createCmdrsEntry(cmdrsEntry);
    }
    
    public CmdrsEntry createCmdrsEntry(String json) {
        return cmdrsDAO.createCmdrsEntry(json);
    }


    public boolean deleteCmdrsEntry(@NonNull String id) {
        return cmdrsDAO.deleteCmdrsEntry(id);
    }

    public CmdrsEntry getCmdrsEntryById(@NonNull String id) {
        return cmdrsDAO.readCmdrsEntry(id);
    }

    public List<CmdrsEntry> getCmdrsEntries() {
        return cmdrsDAO.readCmdrsEntries();
    }

    public CmdrsEntry updateCmdrsEntry(@NonNull String id, boolean isComplete) {
        return cmdrsDAO.updateCmdrsEntry(id, isComplete);
    }
}
