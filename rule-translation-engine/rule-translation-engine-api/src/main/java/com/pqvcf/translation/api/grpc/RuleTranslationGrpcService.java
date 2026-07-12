package com.pqvcf.translation.api.grpc;

import com.pqvcf.translation.application.port.in.GetTranslatedRuleUseCase;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslateRuleCommand;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslationResponse;
import com.pqvcf.translation.grpc.RulesListResponse;
import com.pqvcf.translation.grpc.RulesRequest;
import com.pqvcf.translation.grpc.RuleTranslationServiceGrpc;
import com.pqvcf.translation.grpc.TranslationDetailResponse;
import com.pqvcf.translation.grpc.TranslationRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleTranslationGrpcService extends RuleTranslationServiceGrpc.RuleTranslationServiceImplBase {

    private final TranslateRuleUseCase translateUseCase;
    private final GetTranslatedRuleUseCase getUseCase;

    public RuleTranslationGrpcService(
            TranslateRuleUseCase translateUseCase,
            GetTranslatedRuleUseCase getUseCase) {
        this.translateUseCase = translateUseCase;
        this.getUseCase = getUseCase;
    }

    @Override
    public void translateCNLRule(TranslationRequest request, StreamObserver<TranslationDetailResponse> responseObserver) {
        try {
            TranslateRuleCommand command = new TranslateRuleCommand(
                    request.getRegulationShortName(),
                    request.getArticleNumber(),
                    request.getClauseNumber(),
                    request.getRawSourceText()
            );

            TranslationResponse res = translateUseCase.translate(command);
            TranslationDetailResponse response = mapToDetail(res);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to translate rule: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRulesForRegulation(RulesRequest request, StreamObserver<RulesListResponse> responseObserver) {
        try {
            List<TranslationResponse> rules = getUseCase.findByRegulation(request.getRegulationShortName());
            
            List<TranslationDetailResponse> detailResponses = rules.stream()
                    .map(this::mapToDetail)
                    .collect(Collectors.toList());

            RulesListResponse response = RulesListResponse.newBuilder()
                    .addAllRules(detailResponses)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to fetch rules: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private TranslationDetailResponse mapToDetail(TranslationResponse res) {
        return TranslationDetailResponse.newBuilder()
                .setId(res.id())
                .setRegulationShortName(res.regulationShortName())
                .setArticleNumber(res.articleNumber())
                .setClauseNumber(res.clauseNumber() != null ? res.clauseNumber() : "")
                .setRawSourceText(res.rawSourceText())
                .setDeonticOperator(res.deonticOperator())
                .setSubject(res.subject())
                .setAction(res.action())
                .setTarget(res.target())
                .setConstraint(res.constraint() != null ? res.constraint() : "")
                .setSmtSpec(res.smtSpec())
                .setOdrlPolicy(res.odrlPolicy())
                .setIsOrderValid(res.isValid()) // Note: field name maps to protocol standard
                .setIsValid(res.isValid())
                .setValidationMessage(res.validationMessage() != null ? res.validationMessage() : "")
                .build();
    }
}
