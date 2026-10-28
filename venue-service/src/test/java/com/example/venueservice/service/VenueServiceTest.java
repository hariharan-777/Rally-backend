package com.example.venueservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.venueservice.dto.VenueDtos.VenueSearchRequest;
import com.example.venueservice.model.Sport;
import com.example.venueservice.repository.VenueRepository;
import org.junit.jupiter.api.Test;

class VenueServiceTest {
    private final VenueRepository repository = mock(VenueRepository.class);
    private final VenueService service = new VenueService(repository);

    @Test
    void shortTextUsesPrefixSearch() {
        service.search(new VenueSearchRequest(" Sm ", null, null, null, null, null));

        verify(repository).searchByPrefix(eq("sm%"), eq(false), eq(""), eq(false),
                anyDouble(), anyDouble(), anyDouble(), eq(20));
        verify(repository, never()).searchByName(anyString(), anyString(), anyBoolean(), anyString(),
                anyBoolean(), anyDouble(), anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void longerTextUsesTrigramSearchWithFilters() {
        service.search(new VenueSearchRequest("Smash", Sport.BADMINTON, 12.9352, 77.6245, 3.0, 10));

        verify(repository).searchByName("%smash%", "smash", true, "BADMINTON", true,
                12.9352, 77.6245, 3000.0, 10);
    }

    @Test
    void likeWildcardsInTheTextAreEscaped() {
        service.search(new VenueSearchRequest("50%_off", null, null, null, null, null));

        verify(repository).searchByName(eq("%50\\%\\_off%"), eq("50%_off"), anyBoolean(), anyString(),
                anyBoolean(), anyDouble(), anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void locationWithoutTextFindsNearbyVenuesWithinDefaultRadius() {
        service.search(new VenueSearchRequest(null, null, 12.9352, 77.6245, null, null));

        verify(repository).findNearby(false, "", true, 12.9352, 77.6245, 5000.0, 20);
    }

    @Test
    void rejectsSearchWithNeitherTextNorLocation() {
        assertThatThrownBy(() -> service.search(new VenueSearchRequest("  ", null, null, null, null, null)))
                .isInstanceOf(VenueService.InvalidSearchException.class);
    }

    @Test
    void rejectsLatitudeWithoutLongitude() {
        assertThatThrownBy(() -> service.search(new VenueSearchRequest("smash", null, 12.9, null, null, null)))
                .isInstanceOf(VenueService.InvalidSearchException.class)
                .hasMessageContaining("together");
    }

    @Test
    void escapeLikeLeavesPlainTextAlone() {
        assertThat(VenueService.escapeLike("smash arena")).isEqualTo("smash arena");
    }
}
