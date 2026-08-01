package com.talentiq.service;

import com.talentiq.dto.request.MatchRequest;
import com.talentiq.dto.response.MatchResponse;

public interface MatchingService {
    MatchResponse match(MatchRequest request);
}
