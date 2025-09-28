package com.justlife.bookingservice.service.impl;

import com.justlife.bookingservice.model.Worker;
import com.justlife.bookingservice.repository.WorkerRepository;
import com.justlife.bookingservice.service.IWorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkerService implements IWorkerService {

    private final WorkerRepository workerRepository;

    @Override
    public Optional<Worker> getWorkerById(Long id) {
        return workerRepository.findById(id);
    }

    @Override
    public Worker createWorker(Worker worker) {
        return workerRepository.save(worker);
    }

    @Override
    public Optional<Worker> updateWorker(Long id, Worker worker) {
        return workerRepository.findById(id).map(existingProfessional -> {
            worker.setId(id);
            return workerRepository.save(worker);
        });
    }

    @Override
    public boolean deleteWorker(Long id) {
        return workerRepository.findById(id).map(professional -> {
            workerRepository.deleteById(id);
            return true;
        }).orElse(false);
    }

    @Override
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }
}
