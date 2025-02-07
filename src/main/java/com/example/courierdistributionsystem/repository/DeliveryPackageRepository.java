package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPackageRepository extends JpaRepository<DeliveryPackage, Long> {

    /**
     * Find all delivery packages for a specific customer.
     *
     * @param customer The customer whose packages to find
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByCustomer(Customer customer);

    /**
     * Find all delivery packages for a specific customer, ordered by creation date descending.
     *
     * @param customer The customer whose packages to find
     * @param pageable Pagination information
     * @return Page of delivery packages
     */
    Page<DeliveryPackage> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    /**
     * Find all delivery packages assigned to a specific courier.
     *
     * @param courier The courier whose assigned packages to find
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByCourier(Courier courier);

    /**
     * Find all delivery packages assigned to a specific courier, ordered by creation date descending.
     *
     * @param courier The courier whose assigned packages to find
     * @param pageable Pagination information
     * @return Page of delivery packages
     */
    Page<DeliveryPackage> findByCourierOrderByCreatedAtDesc(Courier courier, Pageable pageable);

    /**
     * Find all delivery packages with a specific status.
     *
     * @param status The status to filter by
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByStatus(DeliveryPackage.DeliveryStatus status);

    /**
     * Find all delivery packages with a specific status, ordered by creation date descending.
     *
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return Page of delivery packages
     */
    Page<DeliveryPackage> findByStatusOrderByCreatedAtDesc(DeliveryPackage.DeliveryStatus status, Pageable pageable);

    /**
     * Find all delivery packages for a specific customer with a given status.
     *
     * @param customer The customer whose packages to find
     * @param status The status to filter by
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByCustomerAndStatus(Customer customer, DeliveryPackage.DeliveryStatus status);

    /**
     * Find all delivery packages for a specific customer with given statuses.
     *
     * @param customer The customer whose packages to find
     * @param statuses The list of statuses to filter by
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByCustomerAndStatusIn(Customer customer, List<DeliveryPackage.DeliveryStatus> statuses);

    /**
     * Find all delivery packages assigned to a specific courier with a given status.
     *
     * @param courier The courier whose assigned packages to find
     * @param status The status to filter by
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByCourierAndStatus(Courier courier, DeliveryPackage.DeliveryStatus status);

    /**
     * Find all delivery packages assigned to a specific courier with given statuses.
     *
     * @param courier The courier whose assigned packages to find
     * @param statuses The list of statuses to filter by
     * @return List of delivery packages
     */
    List<DeliveryPackage> findByCourierAndStatusIn(Courier courier, List<DeliveryPackage.DeliveryStatus> statuses);

    /**
     * Find all delivery packages that are pending assignment to a courier.
     *
     * @return List of unassigned delivery packages
     */
    List<DeliveryPackage> findByCourierIsNullAndStatus(DeliveryPackage.DeliveryStatus status);

    /**
     * Find all delivery packages created within a specific date range.
     *
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of delivery packages
     */
    @Query("SELECT dp FROM DeliveryPackage dp WHERE dp.createdAt BETWEEN :startDate AND :endDate ORDER BY dp.createdAt DESC")
    List<DeliveryPackage> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find all delivery packages in a specific status that have been updated before a given time.
     *
     * @param status The status to filter by
     * @param beforeTime The time threshold
     * @return List of delivery packages
     */
    @Query("SELECT dp FROM DeliveryPackage dp WHERE dp.status = :status AND dp.updatedAt < :beforeTime")
    List<DeliveryPackage> findByStatusAndUpdatedBefore(
            @Param("status") DeliveryPackage.DeliveryStatus status,
            @Param("beforeTime") LocalDateTime beforeTime);

    /**
     * Find all delivery packages assigned to a courier within a specific date range.
     *
     * @param courier The courier whose assigned packages to find
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of delivery packages
     */
    @Query("SELECT dp FROM DeliveryPackage dp WHERE dp.courier = :courier AND dp.createdAt BETWEEN :startDate AND :endDate")
    List<DeliveryPackage> findByCourierAndDateRange(
            @Param("courier") Courier courier,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all delivery packages for a customer within a specific date range.
     *
     * @param customer The customer whose packages to find
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of delivery packages
     */
    @Query("SELECT dp FROM DeliveryPackage dp WHERE dp.customer = :customer AND dp.createdAt BETWEEN :startDate AND :endDate")
    List<DeliveryPackage> findByCustomerAndDateRange(
            @Param("customer") Customer customer,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find delivery packages by tracking number.
     *
     * @param trackingNumber The tracking number to search for
     * @return Optional containing the delivery package if found
     */
    Optional<DeliveryPackage> findByTrackingNumber(String trackingNumber);

    /**
     * Count delivery packages by status for a specific courier.
     *
     * @param courier The courier to count packages for
     * @param status The status to filter by
     * @return Number of packages
     */
    long countByCourierAndStatus(Courier courier, DeliveryPackage.DeliveryStatus status);

    /**
     * Count delivery packages by status for a specific customer.
     *
     * @param customer The customer to count packages for
     * @param status The status to filter by
     * @return Number of packages
     */
    long countByCustomerAndStatus(Customer customer, DeliveryPackage.DeliveryStatus status);

    List<DeliveryPackage> findByStatusAndCourierIsNull(DeliveryPackage.DeliveryStatus status);
} 