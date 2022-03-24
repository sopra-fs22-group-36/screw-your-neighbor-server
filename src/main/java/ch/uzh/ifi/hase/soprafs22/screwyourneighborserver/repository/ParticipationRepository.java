package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.ParticipationEmbedProjection;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = ParticipationEmbedProjection.class)
public interface ParticipationRepository extends PagingAndSortingRepository<Participation, Long> {}
