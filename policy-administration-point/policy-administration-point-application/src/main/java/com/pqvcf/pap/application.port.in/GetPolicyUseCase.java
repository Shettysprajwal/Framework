package com.pqvcf.pap.application.port.in;

import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.PolicyResponse;
import java.util.List;
import java.util.Optional;

public interface GetPolicyUseCase {
    Optional<PolicyResponse> getById(String id);
    List<PolicyResponse> listAll();
    void delete(String id);
    PolicyResponse activate(String id);
    PolicyResponse deprecate(String id);
}
