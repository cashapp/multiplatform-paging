import UIKit
import shared

class PagingDataCollector: Kotlinx_coroutines_coreFlowCollector {
  
  private let pagingCollectionViewController: Paging_runtime_uikitPagingCollectionViewController<Repository>
  
  init(pagingCollectionViewController: Paging_runtime_uikitPagingCollectionViewController<Repository>) {
    self.pagingCollectionViewController = pagingCollectionViewController
  }
  
  func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
    let pagingData = value as! Paging_commonPagingData<Repository>
    DispatchQueue.main.async {
      self.pagingCollectionViewController.submitData(pagingData: pagingData, completionHandler: {_ in print("completed PagingDataCollector")})
    }
  }
}

class ViewModelCollector: Kotlinx_coroutines_coreFlowCollector {
  
  private let pagingCollectionViewController: Paging_runtime_uikitPagingCollectionViewController<Repository>
  
  init(pagingCollectionViewController: Paging_runtime_uikitPagingCollectionViewController<Repository>) {
    self.pagingCollectionViewController = pagingCollectionViewController
  }
  
  func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
    switch (value as! ViewModel) {
      case is ViewModelSearchResults:
        let viewModel = value as! ViewModelSearchResults
        DispatchQueue.main.async {
          viewModel.repositories.collect(collector: PagingDataCollector(pagingCollectionViewController: self.pagingCollectionViewController), completionHandler: {_ in print("completed ViewModelCollector")})
        }
      default:
        print("Unsupported ViewModel:", value)
    }
  }
}

final class RepositoriesViewController: UICollectionViewController {
  private let presenter = RepoSearchPresenter()
  
  private let delegate = Paging_runtime_uikitPagingCollectionViewController<Repository>()
  
  private let events = ExposedKt.mutableSharedFlow(extraBufferCapacity: Int32.max)
  
  required init(coder: NSCoder) {
    super.init(coder: coder)!
    presenter.produceViewModels(events: events, completionHandler: {viewModels,_ in
      viewModels?.collect(collector: ViewModelCollector(pagingCollectionViewController: self.delegate), completionHandler: {_ in print("completed")})
    })
  }

  override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return Int(delegate.collectionView(collectionView: collectionView, numberOfItemsInSection: Int64(section)))
  }

  override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "RepositoryCell", for: indexPath) as! RepositoryCell

    let repository = delegate.getItem(position: Int32(indexPath.row))!
    cell.fullName.text = repository.fullName
    cell.stargazersCount.text = String(repository.stargazersCount)

    return cell
  }
}

// MARK: - Text Field Delegate
extension RepositoriesViewController: UITextFieldDelegate {
  func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    let activityIndicator = UIActivityIndicatorView(style: .gray)
    textField.addSubview(activityIndicator)
    activityIndicator.frame = textField.bounds
    activityIndicator.startAnimating()

    self.collectionView?.reloadData()

    activityIndicator.removeFromSuperview()

    events.emit(value: EventSearchTerm(searchTerm: textField.text!), completionHandler: {error in
      print("error", error ?? "null")
    })

    presenter.produceViewModels(events: events, completionHandler: {viewModels,_ in
      viewModels?.collect(collector: ViewModelCollector(pagingCollectionViewController: self.delegate), completionHandler: {_ in print("completed")})
    })

    textField.resignFirstResponder()
    return true
  }
}
