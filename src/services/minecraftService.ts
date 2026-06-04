
export interface VersionManifest {
  latest: {
    release: string;
    snapshot: string;
  };
  versions: VersionItem[];
}

export interface VersionItem {
  id: string;
  type: 'release' | 'snapshot' | 'old_beta' | 'old_alpha';
  url: string;
  time: string;
  releaseTime: string;
}

export const fetchVersions = async (): Promise<VersionManifest> => {
  try {
    const response = await fetch('https://launchermeta.mojang.com/mc/game/version_manifest.json');
    if (!response.ok) throw new Error('Failed to fetch version manifest');
    return await response.ok ? response.json() : null;
  } catch (error) {
    console.error('Error fetching versions:', error);
    // Fallback to static if offline or blocked
    return {
      latest: { release: '1.20.4', snapshot: '24w14a' },
      versions: [
        {
          id: '1.20.4',
          type: 'release',
          url: 'https://piston-meta.mojang.com/v1/packages/8c544bd05b22bbf5a477817eb48b99d63c5a6d51/1.20.4.json',
          time: '2023-12-07T14:48:42+00:00',
          releaseTime: '2023-12-07T14:42:04+00:00'
        },
        {
          id: '1.20.1',
          type: 'release',
          url: 'https://piston-meta.mojang.com/v1/packages/5cc14b09ebec6ca6c1c8f13e7b411d33cb411d33/1.20.1.json',
          time: '2023-06-12T11:42:04+00:00',
          releaseTime: '2023-06-12T11:39:24+00:00'
        },
        {
          id: '1.19.4',
          type: 'release',
          url: 'https://piston-meta.mojang.com/v1/packages/b7c25091ff6b34ff3d1f3b392a953e5e40624e52/1.19.4.json',
          time: '2023-03-14T11:56:56+00:00',
          releaseTime: '2023-03-14T11:51:30+00:00'
        },
        {
          id: '1.19.2',
          type: 'release',
          url: 'https://piston-meta.mojang.com/v1/packages/b763ec94812a64c4897f26198f4841961e68b350/1.19.2.json',
          time: '2022-08-18T10:04:12+00:00',
          releaseTime: '2022-08-18T10:01:24+00:00'
        },
        {
          id: '1.18.2',
          type: 'release',
          url: 'https://piston-meta.mojang.com/v1/packages/93ecaf0114032d8e05c56c2f9d511ea6cd75a004/1.18.2.json',
          time: '2022-02-28T10:48:07+00:00',
          releaseTime: '2022-02-28T10:48:07+00:00'
        },
        {
          id: '1.16.5',
          type: 'release',
          url: 'https://piston-meta.mojang.com/v1/packages/e1c7f0b5d92e59df95b3582e0ea1ea1c8cff41b5/1.16.5.json',
          time: '2021-01-15T11:44:31+00:00',
          releaseTime: '2021-01-15T11:42:43+00:00'
        }
      ]
    };
  }
};

export const fetchVersionDetails = async (url: string) => {
  const response = await fetch(url);
  return await response.json();
};
