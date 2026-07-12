package com.pqvcf.regulation.api.grpc;

import com.pqvcf.regulation.application.dto.RegulationResponse;
import com.pqvcf.regulation.application.port.in.GetRegulationUseCase;
import com.pqvcf.regulation.domain.repository.JurisdictionGraphRepository;
import com.pqvcf.regulation.grpc.AdequacyRequest;
import com.pqvcf.regulation.grpc.AdequacyResponse;
import com.pqvcf.regulation.grpc.ArticleDetail;
import com.pqvcf.regulation.grpc.ClauseDetail;
import com.pqvcf.regulation.grpc.RegulationDetailResponse;
import com.pqvcf.regulation.grpc.RegulationRequest;
import com.pqvcf.regulation.grpc.RegulationServiceGrpc;
import com.pqvcf.shared.types.JurisdictionCode;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class RegulationGrpcService extends RegulationServiceGrpc.RegulationServiceImplBase {

    private final GetRegulationUseCase getRegulationUseCase;
    private final JurisdictionGraphRepository graphRepository;

    public RegulationGrpcService(
            GetRegulationUseCase getRegulationUseCase,
            JurisdictionGraphRepository graphRepository) {
        this.getRegulationUseCase = getRegulationUseCase;
        this.graphRepository = graphRepository;
    }

    @Override
    public void getRegulationByShortName(RegulationRequest request, StreamObserver<RegulationDetailResponse> responseObserver) {
        String shortName = request.getShortName();
        var regulationOpt = getRegulationUseCase.getByShortName(shortName);

        if (regulationOpt.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Regulation with short name " + shortName + " not found")
                    .asRuntimeException());
            return;
        }

        RegulationResponse reg = regulationOpt.get();

        var articlesList = reg.articles().stream().map(art -> {
            var clausesList = art.clauses().stream().map(cls -> 
                ClauseDetail.newBuilder()
                    .setId(cls.id())
                    .setClauseNumber(cls.clauseNumber())
                    .setContent(cls.content())
                    .setClauseType(cls.clauseType())
                    .build()
            ).collect(Collectors.toList());

            return ArticleDetail.newBuilder()
                    .setId(art.id())
                    .setArticleNumber(art.articleNumber())
                    .setTitle(art.title())
                    .setContent(art.content())
                    .setDeonticFormula(art.deonticFormula() != null ? art.deonticFormula() : "")
                    .setOdrlPolicy(art.odrlPolicy() != null ? art.odrlPolicy() : "")
                    .addAllClauses(clausesList)
                    .build();
        }).collect(Collectors.toList());

        var response = RegulationDetailResponse.newBuilder()
                .setId(reg.id())
                .setName(reg.name())
                .setShortName(reg.shortName())
                .setPrimaryJurisdiction(reg.primaryJurisdiction())
                .setVersion(reg.version())
                .setStatus(reg.status())
                .setDescription(reg.description() != null ? reg.description() : "")
                .setFormalSpec(reg.formalSpec() != null ? reg.formalSpec() : "")
                .addAllArticles(articlesList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void checkJurisdictionAdequacy(AdequacyRequest request, StreamObserver<AdequacyResponse> responseObserver) {
        try {
            JurisdictionCode source = JurisdictionCode.of(request.getSourceJurisdiction());
            JurisdictionCode target = JurisdictionCode.of(request.getTargetJurisdiction());
            boolean isAdequate = graphRepository.isAdequate(source, target);

            AdequacyResponse response = AdequacyResponse.newBuilder()
                    .setIsAdequate(isAdequate)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid jurisdiction code formats: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
