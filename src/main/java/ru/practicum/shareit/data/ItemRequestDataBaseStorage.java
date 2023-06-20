package ru.practicum.shareit.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public interface ItemRequestDataBaseStorage extends JpaRepository<ItemRequest, Long> {
    @Query(value = "select r " +
            "from ItemRequest as r " +
            "order by r.created desc")
    List<ItemRequest> getRequestsSortedByDate();

    @Query(value = "select r " +
            "from ItemRequest as r " +
            "where r.userId = ?1 " +
            "order by r.created desc")
    List<ItemRequest> getRequestsSortedByDateAndFindByUser(long userId);

    @Query(value = "select r " +
            "from ItemRequest as r " +
            "order by r.created desc")
    List<ItemRequest> getRequestsSortedByDate(Pageable pageable);

    @Query(value = "select r " +
            "from ItemRequest as r " +
            "where not r.userId = ?1 " +
            "order by r.created desc")
    List<ItemRequest> getRequestsSortedByDateExcludeUser(long requestedBy, Pageable pageable);


}
