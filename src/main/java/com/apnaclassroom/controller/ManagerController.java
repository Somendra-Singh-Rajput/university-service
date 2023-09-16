package com.apnaclassroom.controller;

import com.apnaclassroom.model.management.Manager;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.service.ManagerService;
import com.mysql.cj.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/manager")
@Tag(name = "Management")
public class ManagerController {

    private static final Logger LOG = LoggerFactory.getLogger(ManagerController.class);

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public RegisterResponse saveManager(@RequestBody Manager manager,
                                      @AuthenticationPrincipal User user) {
        RegisterResponse registerResponse = null;
        LOG.info("Inside saving manager data method");
        if(Objects.nonNull(manager) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = managerService.saveManagerData(manager, user.getUsername());
        }
        return registerResponse;
    }

    @Operation(description = "Get endpoint for manager", summary = "This is a summary for management get endpoint",
            responses = {@ApiResponse(description = "Success", responseCode = "200"),
                    @ApiResponse(description = "Unauthorized / Invalid Token", responseCode = "403")
            }
    )
    @GetMapping(path = "/get/{managerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Manager getManagerDataById(@PathVariable String managerId){
        Manager manager = null;
        LOG.info("Inside fetching manager data method for managerId: {}", managerId);

        if(!StringUtils.isNullOrEmpty(managerId)){
            manager =  managerService.getManagerDataById(managerId);
        }
        return manager;
    }

    @GetMapping(path = "/getAllManagersData", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Manager> getAllManagersData(){
        LOG.info("Inside fetching all managers data method");
        return managerService.getAllManagersData();
    }

    @DeleteMapping(value = "/delete/{managerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public RegisterResponse deleteAdminDataById(@PathVariable String managerId,
                                                @AuthenticationPrincipal User user){
        RegisterResponse registerResponse = null;
        LOG.info("Inside deleting manager data method for managerId: {}", managerId);
        if(!StringUtils.isNullOrEmpty(managerId) && !StringUtils.isNullOrEmpty(user.getUsername())){
            registerResponse = managerService.deleteManagerDataById(managerId, user.getUsername());
        }
        return registerResponse;
    }

    @PutMapping(value = "/update/{managerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Manager updateTeacherDataById(@RequestBody Manager manager, @PathVariable String managerId,
                                       @AuthenticationPrincipal User user) {
        Manager updatedManager = new Manager();
        LOG.info("Inside update manager data method");
        if(Objects.nonNull(manager) && !StringUtils.isNullOrEmpty(managerId)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            updatedManager = managerService.updateManagerDataById(manager, managerId, user.getUsername());
        } else{
            LOG.info("Manager data or managerId is either empty or null");
        }
        return updatedManager;
    }
}
