package com.makeyourjurney.presentation.controller;

import com.makeyourjurney.application.actor.BudgetActor;
import com.makeyourjurney.domain.model.BudgetSummary;
import com.makeyourjurney.presentation.dto.request.BudgetCalculateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/budget")
public class BudgetController {

    private final BudgetActor budgetActor;

    public BudgetController(BudgetActor budgetActor) {
        this.budgetActor = budgetActor;
    }

    @PostMapping("/calculate")
    public BudgetSummary calculate(@Valid @RequestBody BudgetCalculateRequest request) {
        return budgetActor.run(request.toActorInput());
    }
}
