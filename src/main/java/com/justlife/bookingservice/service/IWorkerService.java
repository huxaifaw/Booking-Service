package com.justlife.bookingservice.service;

import com.justlife.bookingservice.model.Worker;

import java.util.List;
import java.util.Optional;

public interface IWorkerService {

    Optional<Worker> getWorkerById(Long id);

    Worker createWorker(Worker worker);

    Optional<Worker> updateWorker(Long id, Worker worker);

    boolean deleteWorker(Long id);

    List<Worker> getAllWorkers();
}
